package me.odinmain.features.impl.render

import me.odinmain.features.Module
import me.odinmain.utils.render.Colors
import me.odinmain.utils.render.RenderUtils
import me.odinmain.utils.round
import me.odinmain.utils.ui.getTextWidth
import net.minecraft.network.play.client.C07PacketPlayerDigging
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent

object BPSDisplay : Module(
    name = "BPS Display",
    description = "Displays how many blocks per second you're breaking."
) {
    private var lastBrokenBlock = 0L
    private var blocksBroken = 0
    private var isBreaking = false
    private var startTime = 0L
    private var bps = 0.0

    private val hud by HUD("Display", "Shows the blocks per second you're breaking.") {
        RenderUtils.drawText("§7BPS: §r${if (it) 17.8 else bps.round(1)}", 1f, 1f, Colors.WHITE)
        getTextWidth("BPS: 17.5") + 2f to 10f
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