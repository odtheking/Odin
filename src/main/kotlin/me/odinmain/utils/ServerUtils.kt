package me.odinmain.utils

import me.odinmain.OdinMain.mc
import me.odinmain.events.impl.PacketEvent
import me.odinmain.utils.clock.Executor
import me.odinmain.utils.clock.Executor.Companion.register
import me.odinmain.utils.skyblock.sendCommand
import net.minecraft.network.play.server.S02PacketChat
import net.minecraft.network.play.server.S03PacketTimeUpdate
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object ServerUtils {

    var averageTps = 20f
        private set
    var averagePing = 0f
        private set

    private val unknownCommandRegex = Regex("^/?Unknown command\\. Type \"/?help\" for help\\. \\('odingetpingcommand-----'\\)$")
    private var pingStartTime = 0L
    private var isPinging = false
    private var prevTime = 0L

    init {
        Executor(2000, "ServerUtils") {
            if (mc.isSingleplayer) return@Executor
            pingStartTime = System.nanoTime()
            isPinging = true

            sendCommand("odingetpingcommand-----")
        }.register()
    }

    @SubscribeEvent
    fun onPacket(event: PacketEvent.Receive) {
        when (event.packet) {
            is S02PacketChat -> {
                if (event.packet.chatComponent?.unformattedText?.noControlCodes?.let { unknownCommandRegex.matches(it) } == false) return
                averagePing = (System.nanoTime() - pingStartTime) / 1e6f
                event.isCanceled = true
                isPinging = false
            }

            is S03PacketTimeUpdate -> {
                if (prevTime != 0L)
                    averageTps = (20_000f / (System.currentTimeMillis() - prevTime + 1)).coerceIn(0f, 20f)

                prevTime = System.currentTimeMillis()
            }
            else -> return
        }
    }

    @SubscribeEvent
    fun onWorldLoad(event: WorldEvent.Load) {
        averagePing = 0f
        averageTps = 20f
        prevTime = 0L
    }
}