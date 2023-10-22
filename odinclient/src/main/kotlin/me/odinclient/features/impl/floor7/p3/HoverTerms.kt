package me.odinclient.features.impl.floor7.p3

import me.odinclient.utils.skyblock.PlayerUtils.ClickType
import me.odinclient.utils.skyblock.PlayerUtils.windowClick
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.impl.floor7.p3.TerminalSolver
import me.odinmain.features.settings.impl.DualSetting
import me.odinmain.features.settings.impl.NumberSetting
import me.odinmain.utils.clock.Clock
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.inventory.ContainerChest
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object HoverTerms : Module(
    name = "Hover Terms",
    description = "Clicks the hovered item in a terminal if it is correct.",
    category = Category.FLOOR7,
    tag = TagType.NEW
) {
    private val triggerDelay: Long by NumberSetting("Delay", 200L, 50, 800)
    private val firstClickDelay: Long by NumberSetting("First Click Delay", 200L, 50, 500)
    private val middleClick: Boolean by DualSetting("Click Type", "Left", "Middle", default = true, description = "What Click to use")
    private val triggerBotClock = Clock(triggerDelay)

    @SubscribeEvent
    fun onRenderWorld(event: RenderGameOverlayEvent.Pre) {
        if (
            TerminalSolver.solution.isEmpty() ||
            mc.currentScreen !is GuiChest ||
            !enabled ||
            !triggerBotClock.hasTimePassed(triggerDelay) ||
            System.currentTimeMillis() - TerminalSolver.openedTerminalTime <= firstClickDelay
        ) return
        val gui = mc.currentScreen as GuiChest
        if (gui.inventorySlots !is ContainerChest || gui.slotUnderMouse?.inventory == mc.thePlayer?.inventory) return
        val hoveredItem = gui.slotUnderMouse?.slotIndex ?: return
        if (hoveredItem !in TerminalSolver.solution) return

        if (TerminalSolver.currentTerm == 1) {
            val needed = TerminalSolver.solution.count { it == hoveredItem }
            if (needed >= 3) {
                windowClick(hoveredItem, ClickType.Right)
                triggerBotClock.update()
                return
            }
        } else if (TerminalSolver.currentTerm == 2) {
            if (TerminalSolver.solution.first() == hoveredItem) {
                windowClick(hoveredItem, if (middleClick) ClickType.Middle else ClickType.Left)
                triggerBotClock.update()
            }
            return
        }
        windowClick(hoveredItem, if (middleClick) ClickType.Middle else ClickType.Left)
        triggerBotClock.update()
    }
}