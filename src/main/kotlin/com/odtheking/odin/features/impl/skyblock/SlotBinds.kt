package com.odtheking.odin.features.impl.skyblock

import com.odtheking.odin.clickgui.settings.impl.*
import com.odtheking.odin.clickgui.settings.impl.label
import com.odtheking.odin.events.GuiEvent
import com.odtheking.odin.events.ScreenEvent
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
    private val lineColor by ColorSetting("Bind Color", Colors.MINECRAFT_GREEN, desc = "Color of the line drawn between slots (used in hover modes).")
    private val lineWidth by NumberSetting("Line Width", 0.5f, 0.1..2.0, 0.1, desc = "Width of the line drawn between slots.")
    private val lineDisplayMode by SelectorSetting("Line Display", LineDisplay.HOVER, desc = "When to show lines between bound slots.")
    private val currentProfile by SelectorSetting("Profile", Profile.PROFILE_1, desc = "Select which profile to use.")

    enum class LineDisplay(private val label: String) {
        HOVER("Hover"),
        ON_HOVER_SHIFT("On Hover + Shift"),
        NONE("None");

        override fun toString(): String = label
    }

    enum class Profile { PROFILE_1, PROFILE_2, PROFILE_3, PROFILE_4, PROFILE_5, PROFILE_6 }
    private val profileData by MapSetting("ProfileData", mutableMapOf<String, MutableMap<Int, Int>>())

    private val currentProfileName: String
        get() = currentProfile.label

    private val slotBinds: MutableMap<Int, Int>
        get() = profileData.getOrPut(currentProfileName) { mutableMapOf() }

    private var previousSlot: Int? = null

    init {
        on<GuiEvent.SlotClick> {
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
                modMessage("§aAdded bind from slot §b$slot §ato §d${clickedSlot} §7($currentProfileName).")

                slotBinds[slot] = clickedSlot
                ModuleManager.saveConfigurations()
                previousSlot = null
            } ?: run {
                slotBinds.entries.firstOrNull { it.key == clickedSlot }?.let {
                    slotBinds.remove(it.key)
                    ModuleManager.saveConfigurations()
                    return@on modMessage("§cRemoved bind from slot §b${it.key} §cto §d${it.value} §7($currentProfileName).")
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

            val shouldDraw = when (lineDisplayMode) {
                LineDisplay.HOVER -> previousSlot != null || boundSlot != null
                LineDisplay.ON_HOVER_SHIFT -> previousSlot != null || (boundSlot != null && mc.hasShiftDown())
                LineDisplay.NONE -> previousSlot != null
            }
            if (!shouldDraw) return@on

            guiGraphics.drawLine(startX.toFloat(), startY.toFloat(), endX.toFloat(), endY.toFloat(), lineColor, lineWidth)
        }

        on<ScreenEvent.Close> {
            previousSlot = null
        }
    }
}