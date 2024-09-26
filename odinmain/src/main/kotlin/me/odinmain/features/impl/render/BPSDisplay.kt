package me.odinmain.features.impl.render

import com.github.stivais.ui.color.Color
import com.github.stivais.ui.constraints.percent
import com.github.stivais.ui.constraints.px
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.utils.round
import me.odinmain.utils.ui.and
import net.minecraft.network.play.client.C07PacketPlayerDigging.Action.START_DESTROY_BLOCK
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent
import kotlin.math.roundToInt
import net.minecraft.network.play.client.C07PacketPlayerDigging as PacketPlayerDigging

object BPSDisplay : Module(
    name = "BPS Display",
    description = "Displays how many blocks you're breaking per second."
) {
    private val roundNumber by BooleanSetting("Round number", true, description = "If the number should be rounded.")

    private val hud by HUD(0.percent, 0.percent) {
        text(
            text = "BPS ",
            color = Color.GREEN,
            size = 30.px
        ) and if (preview) text(text = "20.0") else text({ if (roundNumber) bps.roundToInt() else bps.round(1) })
    }.setting("Display", "")

    private var startTime = 0L
    private var isBreaking = false
    private var blocksBroken = 0
    private var lastBrokenBlock = 0L
    private var bps = 0.0


    // this doesn't work properly Ithink Someone please fix
    init {
        onPacket { packet: PacketPlayerDigging ->
            if (packet.status != START_DESTROY_BLOCK) return@onPacket
            if (startTime == 0L) startTime = System.currentTimeMillis()
            isBreaking = true
            blocksBroken++
            lastBrokenBlock = System.currentTimeMillis()
        }
        onEvent<ClientTickEvent> {
            if (!isBreaking) return@onEvent
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
}