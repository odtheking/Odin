package me.odinmain.utils

import me.odinmain.OdinMain.mc
import me.odinmain.events.impl.PacketReceivedEvent
import me.odinmain.utils.clock.Executor
import me.odinmain.utils.clock.Executor.Companion.register
import net.minecraft.entity.Entity
import net.minecraft.network.Packet
import net.minecraft.network.play.client.C16PacketClientStatus
import net.minecraft.network.play.server.*
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object ServerUtils {
    private val packets = ArrayList<Packet<*>>()

    fun handleSendPacket(packet: Packet<*>): Boolean {
        return packets.remove(packet)
    }

    private fun sendPacketNoEvent(packet: Packet<*>) {
        packets.add(packet)
        mc.netHandler?.addToSendQueue(packet)
    }

    private var prevTime = 0L
    var averageTps = 20.0
    var averagePing = 0.0
    private var isPinging = false
    private var pingStartTime = 0L
    val fps get() = mc.debug.split(" ")[0].toIntOrNull() ?: 0

    @SubscribeEvent
    fun onWorldLoad(event: WorldEvent.Load) {
        reset()
    }

    init {
        Executor(2000, "ServerUtils") {
            sendPing()
        }.register()
    }

    @SubscribeEvent
    fun onPacket(event: PacketReceivedEvent) {
        when (event.packet) {
            is S37PacketStatistics -> averagePing = (System.nanoTime() - pingStartTime) / 1e6 * 0.4 + averagePing * 0.6

            is S01PacketJoinGame -> averagePing = 0.0

            is S03PacketTimeUpdate -> {
                if (prevTime != 0L) {
                    averageTps = (20000 / (System.currentTimeMillis() - prevTime + 1f))
                        .coerceIn(0f, 20f) * 0.182 + averageTps * 0.818
                }
                prevTime = System.currentTimeMillis()
            }
            else -> return
        }
        isPinging = false
    }

    private fun sendPing() {
        if (isPinging || mc.thePlayer == null) return
        if (pingStartTime - System.nanoTime() > 10e6) reset()
        pingStartTime = System.nanoTime()
        isPinging = true
        sendPacketNoEvent(C16PacketClientStatus(C16PacketClientStatus.EnumState.REQUEST_STATS))
    }

    fun Entity.getPing(): Int {
        return mc.netHandler.getPlayerInfo(this.uniqueID)?.responseTime ?: -1
    }

    private fun reset() {
        prevTime = 0L
        averageTps = 20.0
        averagePing = 0.0
    }
}