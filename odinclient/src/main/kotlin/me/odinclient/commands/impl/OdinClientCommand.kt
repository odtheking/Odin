package me.odinclient.commands.impl

import me.odinmain.OdinMain.mc
import me.odinmain.commands.commodore
import me.odinmain.utils.skyblock.modMessage
import me.odinmain.utils.smoothRotateTo

val OdinClientCommand = commodore("odinclient") {
    literal("set") {
        runs { yaw: Float, pitch: Float ->
            mc.thePlayer.rotationYaw = yaw.coerceIn(minimumValue = -180f, maximumValue = 180f)
            mc.thePlayer.rotationPitch = pitch.coerceIn(minimumValue = -90f, maximumValue = 90f)
        }
    }

    literal("rotate") {
        runs { yaw: Float, pitch: Float, time: Long? ->
            smoothRotateTo(yaw, pitch, time ?: 100L) { modMessage("§aFinished rotating!") }
        }
    }
}