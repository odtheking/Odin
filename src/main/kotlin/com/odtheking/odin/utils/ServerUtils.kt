package com.odtheking.odin.utils

import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.events.core.onReceive
import net.minecraft.network.protocol.game.ClientboundSetTimePacket
import net.minecraft.network.protocol.ping.ClientboundPongResponsePacket
import net.minecraft.util.Util

object ServerUtils {
    private var prevTime = 0L
    var averageTps = 20f
        private set

    var currentPing: Int = 0
        private set

    var averagePing: Int = 0
        private set

    private const val TPS_HISTORY = 20
    private const val PING_SAMPLES = 20
    private const val MAX_PING = 9999L

    private val tpsLog = FloatArray(TPS_HISTORY)
    private var tpsIndex = 0
    private var tpsCount = 0

    private fun addTpsSample(tps: Float) {
        tpsLog[tpsIndex] = tps
        tpsIndex = (tpsIndex + 1) % TPS_HISTORY
        if (tpsCount < TPS_HISTORY) tpsCount++
    }

    fun getTpsString(): String {
        val count = tpsCount
        if (count == 0) return "Current: ${averageTps.toFixed(1)}"

        // The ring buffer fills from index 0, so the first `count` slots are the recorded samples.
        var max = Float.NEGATIVE_INFINITY
        var min = Float.POSITIVE_INFINITY
        var sum = 0f
        for (i in 0 until count) {
            val tps = tpsLog[i]
            if (tps > max) max = tps
            if (tps < min) min = tps
            sum += tps
        }

        return "Current: ${averageTps.toFixed(1)} (max/min/avg) ${max.toFixed(1)}/${min.toFixed(1)}/${(sum / count).toFixed(1)}"
    }

    init {
        onReceive<ClientboundSetTimePacket> {
            if (prevTime != 0L) {
                averageTps = (20000f / (System.currentTimeMillis() - prevTime + 1)).coerceIn(0f, 20f)
                addTpsSample(averageTps)
            }

            prevTime = System.currentTimeMillis()
        }

        onReceive<ClientboundPongResponsePacket> {
            val ping = Util.getMillis() - time
            if (ping !in 0..MAX_PING) return@onReceive
            currentPing = ping.toInt()

            val pingLog = mc.debugOverlay.pingLogger
            var total = 0L
            var count = 0
            for (i in (pingLog.size() - PING_SAMPLES).coerceAtLeast(0) until pingLog.size()) {
                val sample = pingLog.get(i)
                if (sample in 0..MAX_PING) {
                    total += sample
                    count++
                }
            }
            averagePing = if (count == 0) currentPing else (total / count).toInt()
        }
    }
}
