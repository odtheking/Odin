package me.odinclient.features.impl.floor7.p3

import me.odinclient.utils.skyblock.PlayerUtils
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.impl.floor7.p3.TerminalSolver
import me.odinmain.features.impl.floor7.p3.TerminalTypes
import me.odinmain.features.settings.impl.DualSetting
import me.odinmain.features.settings.impl.NumberSetting
import me.odinmain.utils.clock.Clock
import me.odinmain.utils.getRandom
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.inventory.ContainerChest
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object AutoTerms : Module(
    name = "Auto Terms",
    description = "Automatically completes the terminals in f7/m7.",
    category = Category.FLOOR7,
    tag = TagType.RISKY
) {
    private val autoDelay: Long by NumberSetting("Delay", 200L, 50, 800)
    private val firstClickDelay: Long by NumberSetting("First Click Delay", 200L, 50, 500)
    private val middleClick: Boolean by DualSetting("Click Type", "Left", "Middle", default = true, description = "What Click to use")
    private val clock = Clock(autoDelay)

    @SubscribeEvent
    fun onRenderWorld(event: RenderGameOverlayEvent.Pre) {
        if (
            TerminalSolver.solution.isEmpty() ||
            mc.currentScreen !is GuiChest ||
            !enabled ||
            !clock.hasTimePassed(autoDelay, setTime = true) ||
            System.currentTimeMillis() - TerminalSolver.openedTerminalTime <= firstClickDelay
        ) return
        val gui = mc.currentScreen as GuiChest
        if (gui.inventorySlots !is ContainerChest || gui.slotUnderMouse?.inventory == mc.thePlayer?.inventory) return

        val item = TerminalSolver.solution.getRandom()

        if (TerminalSolver.currentTerm == TerminalTypes.COLOR) {
            val needed = TerminalSolver.solution.count { it == item }
            if (needed >= 3) {
                PlayerUtils.windowClick(item, PlayerUtils.ClickType.Right)
                return
            }
        } else if (TerminalSolver.currentTerm == TerminalTypes.ORDER) {
            PlayerUtils.windowClick(
                TerminalSolver.solution.first(),
                if (middleClick) PlayerUtils.ClickType.Middle else PlayerUtils.ClickType.Left
            )
            return
        }
        PlayerUtils.windowClick(
            item,
            if (middleClick) PlayerUtils.ClickType.Middle else PlayerUtils.ClickType.Left
        )
    }
}