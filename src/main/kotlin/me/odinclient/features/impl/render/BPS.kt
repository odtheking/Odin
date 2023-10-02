package me.odinclient.features.impl.render

import cc.polyfrost.oneconfig.renderer.font.Fonts
import me.odinclient.features.Category
import me.odinclient.features.Module
import me.odinclient.features.settings.impl.HudSetting
import me.odinclient.ui.hud.HudElement
import me.odinclient.utils.Utils.round
import me.odinclient.utils.render.Color
import me.odinclient.utils.render.gui.nvg.getTextWidth
import me.odinclient.utils.render.gui.nvg.text
import net.minecraftforge.event.world.BlockEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent

object BPS : Module(
    name = "Blocks Broken",
    category = Category.RENDER,
    description = "Displays hows many blocks per second you're breaking"
) {

    private var startTime: Long = 0
    private var isBreaking: Boolean = false
    private var blocksBroken: Int = 0
    private var lastBrokenBlock: Long = 0
    private var bps: Double = 0.0

    private val hud: HudElement by HudSetting("Display", 10f, 10f, 2f, false) {
        if (it) { // example
            text("§7BPS: §r17.8", 1f, 9f, Color.WHITE, 16f, Fonts.REGULAR)
            getTextWidth("BPS: 17.8", 16f, Fonts.REGULAR ) to 16f
        } else {
            text("§7BPS: §r${bps.round(1)}", 1f, 9f, Color.WHITE, 16f, Fonts.REGULAR)
            getTextWidth("BPS: ${bps.round(1)}", 16f, Fonts.REGULAR ) to 16f
        }
    }

    @SubscribeEvent
    fun blockBreak(event: BlockEvent.BreakEvent) {
        if (startTime == 0L) startTime = System.currentTimeMillis()
        isBreaking = true
        blocksBroken++
        lastBrokenBlock = System.currentTimeMillis()
    }

    @SubscribeEvent
    fun tick(event: ClientTickEvent) {
        if (!isBreaking) return
        val secondsElapsed = (System.currentTimeMillis() - startTime) / 1000.0
        bps = (blocksBroken / secondsElapsed).round(2)

        if (System.currentTimeMillis() - lastBrokenBlock > 1000 * 1) {
            bps = 0.0
            isBreaking = false
            blocksBroken = 0
            startTime = 0
            lastBrokenBlock = 0
        }
    }
}