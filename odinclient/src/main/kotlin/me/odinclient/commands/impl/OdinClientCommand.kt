package me.odinclient.commands.impl

import me.odinmain.OdinMain.mc
import me.odinmain.commands.commodore

val OdinClientCommand = commodore("odinclient") {
    literal("set") {
        runs { yaw: Float, pitch: Float ->
            mc.thePlayer.rotationYaw = yaw.coerceIn(minimumValue = -180f, maximumValue = 180f)
            mc.thePlayer.rotationPitch = pitch.coerceIn(minimumValue = -90f, maximumValue = 90f)
        }
    }
}