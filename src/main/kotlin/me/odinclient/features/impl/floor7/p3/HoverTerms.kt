package me.odinclient.features.impl.floor7.p3

import me.odinclient.OdinClient.Companion.mc
import me.odinclient.features.Category
import me.odinclient.features.Module
import me.odinclient.features.settings.impl.DualSetting
import me.odinclient.features.settings.impl.NumberSetting
import me.odinclient.utils.skyblock.ChatUtils.modMessage
import me.odinclient.utils.skyblock.PlayerUtils.windowClick
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.inventory.ContainerChest

object HoverTerms : Module(
    name = "Hover Terms",
    description = "Clicks the hovered item in a terminal if it is correct.",
    category = Category.FLOOR7,
    tag = TagType.NEW
) {
    private val delay: Long by NumberSetting<Long>("Delay", 200, 70, 500)
    private val middleClick: Boolean by DualSetting("Click Type", "Left", "Middle", default = true, description = "What Click to use")

    init {
        execute({ delay }) {
            if (TerminalSolver.solution.isEmpty() || mc.currentScreen !is GuiChest || !enabled) return@execute
            val gui = mc.currentScreen as GuiChest
            if (gui.inventorySlots !is ContainerChest || gui.slotUnderMouse.inventory == mc.thePlayer.inventory) return@execute
            val hoveredItem = gui.slotUnderMouse.slotIndex
            if (hoveredItem !in TerminalSolver.solution) return@execute
            windowClick(gui.inventorySlots.windowId, hoveredItem, if (middleClick) 2 else 0, if (middleClick) 3 else 0)
        }
    }
}