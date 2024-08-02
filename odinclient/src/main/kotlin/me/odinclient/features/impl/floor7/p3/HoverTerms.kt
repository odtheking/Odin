package me.odinclient.features.impl.floor7.p3

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.impl.floor7.p3.TerminalSolver
import me.odinmain.features.impl.floor7.p3.TerminalSolver.currentTerm
import me.odinmain.features.impl.floor7.p3.TerminalSolver.openedTerminalTime
import me.odinmain.features.impl.floor7.p3.TerminalTypes
import me.odinmain.features.impl.floor7.p3.termGUI.TermGui
import me.odinmain.features.settings.impl.DualSetting
import me.odinmain.features.settings.impl.NumberSetting
import me.odinmain.ui.util.MouseUtils.mouseX
import me.odinmain.ui.util.MouseUtils.mouseY
import me.odinmain.utils.clock.Clock
import me.odinmain.utils.equalsOneOf
import me.odinmain.utils.skyblock.PlayerUtils
import me.odinmain.utils.skyblock.PlayerUtils.windowClick
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.inventory.ContainerChest
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object HoverTerms : Module(
    name = "Hover Terms",
    description = "Clicks the hovered item in a terminal if it is correct.",
    category = Category.FLOOR7,
    tag = TagType.RISKY
) {
    private val triggerDelay: Long by NumberSetting("Delay", 200L, 50, 800, unit = "ms", description = "Delay between clicks.")
    private val firstClickDelay: Long by NumberSetting("First Click Delay", 200L, 50, 500, unit = "ms", description = "Delay before first click.")
    private val middleClick: Boolean by DualSetting("Click Type", "Left", "Middle", default = true, description = "What Click type to use.")
    private val triggerBotClock = Clock(triggerDelay)

    @SubscribeEvent
    fun onRenderWorld(event: RenderGameOverlayEvent.Pre) {
        if (
            TerminalSolver.solution.isEmpty() ||
            mc.currentScreen !is GuiChest ||
            !enabled ||
            !triggerBotClock.hasTimePassed(triggerDelay) ||
            System.currentTimeMillis() - openedTerminalTime <= firstClickDelay
        ) return
        val gui = mc.currentScreen as GuiChest
        if (gui.inventorySlots !is ContainerChest) return

        val hoveredItem =
            when {
                TerminalSolver.renderType == 3 && TerminalSolver.enabled -> TermGui.getHoveredItem(mouseX.toInt(), mouseY.toInt())
                else -> {
                    if (gui.slotUnderMouse?.inventory == mc.thePlayer?.inventory) return
                    gui.slotUnderMouse?.slotIndex
                }
            } ?: return

        if (hoveredItem !in TerminalSolver.solution) return

        if (currentTerm == TerminalTypes.RUBIX) {
            val needed = TerminalSolver.solution.count { it == hoveredItem }
            if (needed >= 3) {
                windowClick(hoveredItem, PlayerUtils.ClickType.Right)
                triggerBotClock.update()
                return
            }
        } else if (currentTerm == TerminalTypes.ORDER) {
            if (TerminalSolver.solution.first() == hoveredItem) {
                windowClick(hoveredItem, if (middleClick) PlayerUtils.ClickType.Middle else PlayerUtils.ClickType.Left)
                triggerBotClock.update()
            }
            return
        } else if (currentTerm.equalsOneOf(TerminalTypes.PANES, TerminalTypes.STARTS_WITH, TerminalTypes.SELECT))
            windowClick(hoveredItem, if (middleClick) PlayerUtils.ClickType.Middle else PlayerUtils.ClickType.Left)

        triggerBotClock.update()
    }
}