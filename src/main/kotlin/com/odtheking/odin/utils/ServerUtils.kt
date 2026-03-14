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

    init {
        onReceive<ClientboundSetTimePacket> {
            if (prevTime != 0L)
                averageTps = (20000f / (System.currentTimeMillis() - prevTime + 1)).coerceIn(0f, 20f)

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