package me.odinmain.features.impl.skyblock

import me.odinmain.config.Config
import me.odinmain.events.impl.GuiEvent
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.ColorSetting
import me.odinmain.features.settings.impl.KeybindSetting
import me.odinmain.features.settings.impl.MapSetting
import me.odinmain.utils.render.RenderUtils
import me.odinmain.utils.skyblock.PlayerUtils
import me.odinmain.utils.skyblock.modMessage
import me.odinmain.utils.ui.Colors
import net.minecraft.client.gui.inventory.GuiInventory
import net.minecraft.client.renderer.GlStateManager
import net.minecraftforge.client.event.GuiOpenEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.input.Keyboard

object SlotBinds: Module (
    name = "Slot Binds",
    desc = "Bind slots together for quick access.",
    key = null
) {
    private val setNewSlotbind by KeybindSetting("Bind set key", Keyboard.KEY_NONE, description = "Key to set new bindings.")
    private val lineColor by ColorSetting("LineColor", Colors.MINECRAFT_GOLD, desc = "Color of the line drawn between slots.")
    private val slotBinds by MapSetting("slotBinds", mutableMapOf<Int, Int>())

    private var previousSlot: Int? = null

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onGuiClick(event: GuiEvent.MouseClick) {
        if (event.gui !is GuiInventory || !Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) return
        val clickedSlot = event.gui.slotUnderMouse?.slotNumber?.takeIf { it in 5 until 45 } ?: return
        val boundSlot = slotBinds[clickedSlot] ?: return

        val (from, to) = when {
            clickedSlot in 36..44 -> boundSlot to clickedSlot
            boundSlot in 36..44 -> clickedSlot to boundSlot
            else -> return
        }

        PlayerUtils.windowClick(from, to % 36, 2)
        event.isCanceled = true
    }

    @SubscribeEvent
    fun onGuiPress(event: GuiEvent.KeyPress) {
        if (event.gui !is GuiInventory || event.key != setNewSlotbind.key) return
        val clickedSlot = event.gui.slotUnderMouse?.slotNumber?.takeIf { it in 5 until 45 } ?: return

        event.isCanceled = true
        previousSlot?.let { slot ->
            if (previousSlot == clickedSlot) return@let modMessage("§cYou can't bind a slot to itself.")
            if (slot !in 36..44 && clickedSlot !in 36..44) return modMessage("§cOne of the slots must be in the hotbar (36–44).")
            modMessage("§aAdded bind from slot §b$slot §ato §d$clickedSlot.")
            slotBinds[slot] = clickedSlot
            Config.save()
            previousSlot = null
        } ?: run {
                slotBinds.entries.firstOrNull { it.key == clickedSlot }?.let {
                slotBinds.remove(it.key)
                Config.save()
                return modMessage("§cRemoved bind from slot §b${it.key} §cto §d${it.value}.")
            }

            previousSlot = clickedSlot
        }
    }

    @SubscribeEvent
    fun onGuiDraw(event: GuiEvent.DrawGuiForeground) {
        val gui = event.gui as? GuiInventory ?: return
        val hoveredSlot = gui.slotUnderMouse?.slotNumber?.takeIf { it in 5 until 45 } ?: return
        val boundSlotNumber = slotBinds[hoveredSlot]
        val (startX, startY) = gui.inventorySlots?.getSlot(previousSlot ?: hoveredSlot)?.let { slot ->
            slot.xDisplayPosition + event.guiLeft + 8 to slot.yDisplayPosition + event.guiTop + 8 } ?: return

        val (endX, endY) = previousSlot?.let { event.mouseX to event.mouseY } ?: boundSlotNumber?.let { slot ->
            gui.inventorySlots.getSlot(slot)?.let { it.xDisplayPosition + event.guiLeft + 8 to it.yDisplayPosition + event.guiTop + 8 } } ?: return

        if (previousSlot == null && !(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) && boundSlotNumber != null)) return
        GlStateManager.translate(0f, 0f, 999f)
        RenderUtils.drawLine(startX, startY, endX, endY, lineColor, 2f)
        GlStateManager.translate(0f, 0f, -999f)
    }

    @SubscribeEvent
    fun onGuiClose(event: GuiOpenEvent) {
        if (event.gui == null) previousSlot = null
    }
}