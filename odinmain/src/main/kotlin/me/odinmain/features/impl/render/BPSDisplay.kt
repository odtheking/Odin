package me.odinmain.features.impl.render

import me.odinmain.events.impl.PacketSentEvent
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.HudSetting
import me.odinmain.font.OdinFont
import me.odinmain.ui.hud.HudElement
import me.odinmain.ui.util.getTextWidth
import me.odinmain.ui.util.scale
import me.odinmain.ui.util.scaleFactor
import me.odinmain.ui.util.text
import me.odinmain.utils.render.Color
import me.odinmain.utils.round
import net.minecraft.network.play.client.C07PacketPlayerDigging
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent

object BPSDisplay : Module(
    name = "Bps Display",
    category = Category.RENDER,
    description = "Displays how many blocks per second you're breaking."
) {
    private var startTime: Long = 0
    private var isBreaking: Boolean = false
    private var blocksBroken: Int = 0
    private var lastBrokenBlock: Long = 0
    private var bps: Double = 0.0

    private val hud: HudElement by HudSetting("Display", 10f, 10f, 2f, false) {
        scale(1f / scaleFactor, 1f / scaleFactor, 1f)
        if (it) { // example
            text("§7BPS: §r17.8", 1f, 7f, Color.WHITE,10f)
        } else {
            text("§7BPS: §r${bps.round(1)}", 1f, 7f, Color.WHITE, 10f, OdinFont.REGULAR)
        }
        scale(scaleFactor, scaleFactor, 1f)
        getTextWidth("BPS: 17.5", 9f) to 12f
    }

    @SubscribeEvent
    fun packet(event: PacketSentEvent) {
        if (event.packet !is C07PacketPlayerDigging || event.packet.status != C07PacketPlayerDigging.Action.START_DESTROY_BLOCK) return
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
        if (System.currentTimeMillis() - lastBrokenBlock > 1000) {
            bps = 0.0
            isBreaking = false
            blocksBroken = 0
            startTime = 0
            lastBrokenBlock = 0
        }
    }
}