package me.odinmain.features.impl.render

import me.odinmain.features.Module
import me.odinmain.features.settings.impl.HudSetting
import me.odinmain.utils.render.RenderUtils
import me.odinmain.utils.render.getMCTextWidth
import me.odinmain.utils.round
import me.odinmain.utils.ui.Colors
import net.minecraft.network.play.client.C07PacketPlayerDigging
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent

object BPSDisplay : Module(
    name = "BPS Display",
    desc = "Displays how many blocks per second you're breaking."
) {
    private var startTime: Long = 0
    private var isBreaking: Boolean = false
    private var blocksBroken: Int = 0
    private var lastBrokenBlock: Long = 0
    private var bps: Double = 0.0

    private val hud by HudSetting("Display", 10f, 10f, 2f, false) {
        if (it) { // example
            RenderUtils.drawText("§7BPS: §r17.8", 1f, 1f, 1f, Colors.WHITE, center = false)
        } else {
            RenderUtils.drawText("§7BPS: §r${bps.round(1)}", 1f, 1f, 1f, Colors.WHITE, center = false)
        }
        getMCTextWidth("BPS: 17.5") + 2f to 12f
    }

    init {
        onPacket<C07PacketPlayerDigging> {
            if (it.status != C07PacketPlayerDigging.Action.START_DESTROY_BLOCK) return@onPacket
            if (startTime == 0L) startTime = System.currentTimeMillis()
            isBreaking = true
            blocksBroken++
            lastBrokenBlock = System.currentTimeMillis()
        }
    }

    @SubscribeEvent
    fun tick(event: ClientTickEvent) {
        if (!isBreaking) return
        val secondsElapsed = (System.currentTimeMillis() - startTime) / 1000.0
        bps = (blocksBroken / secondsElapsed).round(2).toDouble()
        if (System.currentTimeMillis() - lastBrokenBlock > 1000) {
            bps = 0.0
            isBreaking = false
            blocksBroken = 0
            startTime = 0
            lastBrokenBlock = 0
        }
    }
}