package com.odtheking.odin.features.impl.skyblock

import com.odtheking.odin.clickgui.settings.impl.ColorSetting
import com.odtheking.odin.clickgui.settings.impl.KeybindSetting
import com.odtheking.odin.clickgui.settings.impl.MapSetting
import com.odtheking.odin.clickgui.settings.impl.SelectorSetting
import com.odtheking.odin.events.GuiEvent
import com.odtheking.odin.events.ScreenEvent
import com.odtheking.odin.events.core.EventPriority
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.Module
import com.odtheking.odin.features.ModuleManager
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.clickSlot
import com.odtheking.odin.utils.modMessage
import com.odtheking.odin.utils.render.drawLine
import net.minecraft.client.gui.screens.inventory.InventoryScreen
import net.minecraft.world.inventory.ContainerInput
import org.lwjgl.glfw.GLFW

object SlotBinds : Module(
    name = "Slot Binds",
    description = "Bind slots together for quick access.",
    key = null
) {
    private val setNewSlotbind by KeybindSetting("Bind set key", GLFW.GLFW_KEY_UNKNOWN, desc = "Key to set new bindings.")
    private val lineColor by ColorSetting("Line Color", Colors.MINECRAFT_GOLD, desc = "Color of the line drawn between slots.")
    private val profileOptions = listOf("Profile 1", "Profile 2", "Profile 3", "Profile 4", "Profile 5", "Profile 6")
    private val currentProfile by SelectorSetting("Profile", "Profile 1", profileOptions, desc = "Select which profile to use.")
    private val profileData by MapSetting("ProfileData", mutableMapOf<String, MutableMap<Int, Int>>())

    private val currentProfileName: String
        get() = profileOptions[currentProfile]

    private val slotBinds: MutableMap<Int, Int>
        get() = profileData.getOrPut(currentProfileName) { mutableMapOf() }

    private var previousSlot: Int? = null

    init {
        on<GuiEvent.SlotClick> (EventPriority.HIGHEST) {
            if (!mc.hasShiftDown() || screen !is InventoryScreen) return@on
            val clickedSlot = screen.hoveredSlot?.index?.takeIf { it in 5 until 45 } ?: return@on
            val boundSlot = slotBinds[clickedSlot] ?: return@on

            val (from, to) = when {
                clickedSlot in 36..44 -> boundSlot to clickedSlot
                boundSlot in 36..44 -> clickedSlot to boundSlot
                else -> return@on
            }

            mc.player?.clickSlot(screen.menu.containerId, from, to % 36, ContainerInput.SWAP)
            cancel()
        }

        on<ScreenEvent.KeyPress> {
            if (screen !is InventoryScreen || input.key != setNewSlotbind.value) return@on
            val clickedSlot = screen.hoveredSlot?.index?.takeIf { it in 5 until 45 } ?: return@on

            cancel()
            previousSlot?.let { slot ->
                if (slot == clickedSlot) return@on modMessage("§cYou can't bind a slot to itself.")
                if (slot !in 36..44 && clickedSlot !in 36..44) return@on modMessage("§cOne of the slots must be in the hotbar (36–44).")
                modMessage("§aAdded bind from slot §b$slot §ato §d${clickedSlot} §7(${profileOptions[currentProfile]}).")

                slotBinds[slot] = clickedSlot
                ModuleManager.saveConfigurations()
                previousSlot = null
            } ?: run {
                slotBinds.entries.firstOrNull { it.key == clickedSlot }?.let {
                    slotBinds.remove(it.key)
                    ModuleManager.saveConfigurations()
                    return@on modMessage("§cRemoved bind from slot §b${it.key} §cto §d${it.value} §7(${profileOptions[currentProfile]}).")
                }
                previousSlot = clickedSlot
            }
        }

        on<GuiEvent.DrawTooltip> {
            val screen = screen as? InventoryScreen ?: return@on
            val hoveredSlot = screen.hoveredSlot?.index?.takeIf { it in 5 until 45 } ?: return@on
            val boundSlot = slotBinds[hoveredSlot]

            val (startX, startY) = screen.menu.getSlot(previousSlot ?: hoveredSlot).let { slot ->
                slot.x + screen.leftPos + 8 to slot.y + screen.topPos + 8
            }

            val (endX, endY) = previousSlot?.let { mouseX to mouseY } ?: boundSlot?.let { slot ->
                screen.menu.getSlot(slot).let { it.x + screen.leftPos + 8 to it.y + screen.topPos + 8 }
            } ?: return@on

            if (previousSlot == null && !(mc.hasShiftDown())) return@on

            guiGraphics.drawLine(startX.toFloat(), startY.toFloat(), endX.toFloat(), endY.toFloat(), lineColor, 1f)
        }

        on<ScreenEvent.Close> {
            previousSlot = null
        }
    }
}