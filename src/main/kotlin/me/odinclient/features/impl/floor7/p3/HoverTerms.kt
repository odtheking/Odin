package me.odinclient.features.impl.floor7.p3

import me.odinclient.OdinClient.Companion.mc
import me.odinclient.features.Category
import me.odinclient.features.Module
import me.odinclient.features.impl.dungeon.SecretTriggerbot
import me.odinclient.features.settings.impl.DualSetting
import me.odinclient.features.settings.impl.NumberSetting
import me.odinclient.utils.clock.Clock
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
    private val triggerBotClock = Clock(delay)

    init {
        execute(10) {// Virtually the same thing as a render world trigger, except it will only fire 100fps and under (there is no need for more in this case)
            if (TerminalSolver.solution.isEmpty() || mc.currentScreen !is GuiChest || !enabled || !triggerBotClock.hasTimePassed(delay)) return@execute
            val gui = mc.currentScreen as GuiChest
            if (gui.inventorySlots !is ContainerChest || gui.slotUnderMouse.inventory == mc.thePlayer.inventory) return@execute
            val hoveredItem = gui.slotUnderMouse.slotIndex
            if (hoveredItem !in TerminalSolver.solution) return@execute
            if (TerminalSolver.currentTerm == 1) {
                val needed = TerminalSolver.solution.count { it == hoveredItem }
                if (needed >= 3) {
                    windowClick(gui.inventorySlots.windowId, hoveredItem, 1,0)
                    return@execute
                }
            }
            windowClick(gui.inventorySlots.windowId, hoveredItem, if (middleClick) 2 else 0, if (middleClick) 3 else 0)
            triggerBotClock.update()
        }
    }
}