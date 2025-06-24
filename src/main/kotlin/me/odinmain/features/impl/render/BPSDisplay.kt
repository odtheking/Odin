package me.odinmain.features.impl.render

import com.github.stivais.aurora.color.Color
import me.odinmain.events.impl.PacketEvent
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.utils.round
import me.odinmain.utils.ui.Colors
import net.minecraft.network.play.client.C07PacketPlayerDigging.Action.START_DESTROY_BLOCK
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent
import net.minecraft.network.play.client.C07PacketPlayerDigging as PacketPlayerDigging

object BPSDisplay : Module(
    name = "BPS Display",
    description = "Displays how many blocks you're breaking per second."
) {
    private val roundNumber by BooleanSetting("Round number", true, description = "If the number should be rounded.")

    private var bps = 0.0
        get() = field.coerceIn(0.0, 20.0)

    private var startTime = 0L
    private var isBreaking = false
    private var blocksBroken = 0
    private var lastBrokenBlock = 0L

    @SubscribeEvent
    fun onPacket(event: PacketEvent.Receive) {
        val packet = event.packet as? PacketPlayerDigging ?: return
        if (packet.status != START_DESTROY_BLOCK) return
        if (startTime == 0L) startTime = System.currentTimeMillis()
        isBreaking = true
        blocksBroken++
        lastBrokenBlock = System.currentTimeMillis()
    }

    @SubscribeEvent
    fun onTick(event: ClientTickEvent) {
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

    val getBPSColor: () -> Color = {
        when {
            bps >= 20 -> Colors.MINECRAFT_GREEN
            bps >= 15 -> Colors.MINECRAFT_YELLOW
            bps >= 10 -> Colors.MINECRAFT_GOLD
            bps >= 5 -> Colors.MINECRAFT_RED
            else -> Colors.MINECRAFT_DARK_RED
        } }
}
