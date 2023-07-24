package me.odinclient.utils

import me.odinclient.OdinClient.Companion.mc
import me.odinclient.events.ReceivePacketEvent
import net.minecraft.network.Packet
import net.minecraft.network.play.client.C16PacketClientStatus
import net.minecraft.network.play.server.S01PacketJoinGame
import net.minecraft.network.play.server.S03PacketTimeUpdate
import net.minecraft.network.play.server.S37PacketStatistics
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent

object ServerUtils {

    private val packets = ArrayList<Packet<*>>()

    fun handleSendPacket(packet: Packet<*>): Boolean {
        if (packets.contains(packet)) {
            packets.remove(packet)
            return true
        }
        return false
    }

    fun sendPacketNoEvent(packet: Packet<*>) {
        packets.add(packet)
        mc.netHandler?.addToSendQueue(packet)
    }

    private var prevTime = 0L
    var averageTps = 20.0
    var averagePing = 0.0
    private var isPinging = false
    private var pingStartTime = 0L

    @SubscribeEvent
    fun onWorldLoad(event: WorldEvent.Load) {
        prevTime = 0L
        averageTps = 20.0
        averagePing = 0.0
    }

    private var tickRamp = 20
    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        tickRamp++
        if (tickRamp % 40 != 0) return
        sendPing()
    }

    @SubscribeEvent
    fun onPacket(event: ReceivePacketEvent) {
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
        pingStartTime = System.nanoTime()
        isPinging = true
        sendPacketNoEvent(
            C16PacketClientStatus(
                C16PacketClientStatus.EnumState.REQUEST_STATS
            )
        )
    }
}
