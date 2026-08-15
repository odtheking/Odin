package com.odtheking.odin.utils

import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.events.core.onReceive
import net.minecraft.network.protocol.game.ClientboundSetTimePacket
import net.minecraft.network.protocol.ping.ClientboundPongResponsePacket
import net.minecraft.util.Util
import kotlin.math.min

object ServerUtils {
    private var prevTime = 0L
    var averageTps = 20f
        private set

    var currentPing: Int = 0
        private set

    var averagePing: Int = 0
        private set

    private const val TPS_HISTORY = 20

    private val tpsLog = FloatArray(TPS_HISTORY)
    private var tpsIndex = 0
    private var tpsCount = 0

    private fun addTpsSample(tps: Float) {
        tpsLog[tpsIndex] = tps
        tpsIndex = (tpsIndex + 1) % TPS_HISTORY
        if (tpsCount < TPS_HISTORY) tpsCount++
    }

    fun getTpsString(): String {
        if (tpsLog.isEmpty()) return "Current: ${averageTps.toFixed(1)}"

        return "Current: ${averageTps.toFixed(1)} (max/min/avg) ${tpsLog.max().toFixed(1)}/${tpsLog.min().toFixed(1)}/${tpsLog.average().toFloat().toFixed(1)}"
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
            currentPing = (Util.getMillis() - time).toInt().coerceAtLeast(0)

            val pingLog = mc.debugOverlay.pingLogger

            val sampleSize = min(pingLog.size(), 20)

            if (sampleSize == 0) {
                averagePing = currentPing
                return@onReceive
            }

            var total = 0L
            for (i in 0 until sampleSize) {
                total += pingLog.get(i)
            }

            averagePing = (total / sampleSize).toInt()
        }
    }
}
