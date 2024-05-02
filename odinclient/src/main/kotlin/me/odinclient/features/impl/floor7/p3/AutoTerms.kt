package me.odinclient.features.impl.floor7.p3

import me.odinclient.utils.skyblock.PlayerUtils
import me.odinmain.events.impl.GuiEvent
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.impl.floor7.p3.TerminalSolver
import me.odinmain.features.impl.floor7.p3.TerminalTypes
import me.odinmain.features.settings.impl.DualSetting
import me.odinmain.features.settings.impl.NumberSetting
import me.odinmain.utils.clock.Clock
import net.minecraft.inventory.ContainerChest
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent

object AutoTerms : Module(
    name = "Auto Terms",
    description = "Automatically completes the terminals in f7/m7.",
    category = Category.FLOOR7,
    tag = TagType.RISKY
) {
    private val autoDelay: Long by NumberSetting("Delay", 170L, 130, 300)
    private val firstClickDelay: Long by NumberSetting("First Click Delay", 350L, 300, 500)
    private val middleClick: Boolean by DualSetting("Click Type", "Left", "Middle", default = true, description = "What Click to use")
    private val breakThreshold: Long by NumberSetting("Break Threshold", 500L, 350L, 1000L, 10L)
    private val clock = Clock(autoDelay)
    private var clickedThisWindow = false
    private var breakClock = Clock(breakThreshold)


    @SubscribeEvent
    fun onGuiOpen(event: GuiEvent.GuiLoadedEvent) {
        clickedThisWindow = false
    }

    @SubscribeEvent
    fun onRenderWorld(event: TickEvent.ClientTickEvent) {
        if (breakClock.hasTimePassed(breakThreshold) && clickedThisWindow) {
            clickedThisWindow = false
        }
        if (
            TerminalSolver.solution.isEmpty() ||
            !clock.hasTimePassed(autoDelay) ||
            System.currentTimeMillis() - TerminalSolver.openedTerminalTime <= firstClickDelay ||
            clickedThisWindow ||
            event.phase != TickEvent.Phase.START ||
            mc.thePlayer.openContainer !is ContainerChest
        ) return

        val item = TerminalSolver.solution.first()

        clickedThisWindow = true
        clock.update()
        breakClock.update()
        when (TerminalSolver.currentTerm) {
            TerminalTypes.RUBIX ->  PlayerUtils.windowClick(
                                        item,
                                        if (TerminalSolver.solution.count { it == item } >= 3) PlayerUtils.ClickType.Right else if (middleClick) PlayerUtils.ClickType.Middle else PlayerUtils.ClickType.Left
                                    )


            TerminalTypes.ORDER ->  PlayerUtils.windowClick(
                                        TerminalSolver.solution.first(),
                                        if (middleClick) PlayerUtils.ClickType.Middle else PlayerUtils.ClickType.Left
                                    )

            else -> PlayerUtils.windowClick(item, if (middleClick) PlayerUtils.ClickType.Middle else PlayerUtils.ClickType.Left)
        }
    }
}
