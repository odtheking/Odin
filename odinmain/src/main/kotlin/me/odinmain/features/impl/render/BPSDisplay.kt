package me.odinmain.features.impl.render

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.HudSetting
import me.odinmain.font.OdinFont
import me.odinmain.ui.hud.HudElement
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.getMCTextWidth
import me.odinmain.utils.render.mcText
import me.odinmain.utils.render.text
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
        if (it) { // example
            mcText("§7BPS: §r17.8", 1f, 1f, 1, Color.WHITE, center = false)
        } else {
            mcText("§7BPS: §r17.8", 1f, 1f, 1, Color.WHITE, center = false)
            text("§7BPS: §r${bps.round(1)}", 1f, 7f, Color.WHITE, 10f, OdinFont.REGULAR)
        }
        getMCTextWidth("BPS: 17.5") + 2f to 10f
    }

    init {
        onPacket(C07PacketPlayerDigging::class.java) {
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