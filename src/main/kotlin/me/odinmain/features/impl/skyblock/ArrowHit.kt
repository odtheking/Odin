package me.odinmain.features.impl.skyblock

import me.odinmain.features.Module
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.HudSetting
import me.odinmain.features.settings.impl.StringSetting
import me.odinmain.utils.render.getMCTextWidth
import me.odinmain.utils.render.mcText
import me.odinmain.utils.ui.Colors
import net.minecraft.network.play.server.S29PacketSoundEffect

object ArrowHit : Module(
    name = "Arrow hit",
    description = "Counts how many arrows you hit in certain time periods."
) {
    private val resetOnNumber by BooleanSetting("Reset on number", false, description = "Reset the arrow count after a certain number of arrows.")
    private val resetCount by StringSetting("Reset count", 999999.toString(), 16, description = "The amount of arrows to hit before resetting the count.")
    private val resetOnWorldLoad by BooleanSetting("Reset on world load", true, description = "Reset the arrow count when you join a world.")
    val resetOnDragons by BooleanSetting("Reset on dragon spawn", true, description = "Reset the arrow count when a m7 dragon has spawned.")

    private var arrowCount = 0

    private val hud by HudSetting("Display", 10f, 10f, 2f, false) {
        if (it) {
            mcText("156", 0f, 2f, 1f, Colors.WHITE, center = false)
            getMCTextWidth("156").toFloat() to 12f
        } else {
            mcText("$arrowCount", 0f, 2f, 1f, Colors.WHITE, center = false)
            getMCTextWidth("$arrowCount").toFloat() to 12f
        }
    }
    init {
        onPacket<S29PacketSoundEffect> {
            if (it.soundName != "random.successful_hit") return@onPacket
            arrowCount += 1
            if (arrowCount >= (resetCount.toIntOrNull() ?: 9999) && resetOnNumber) arrowCount = 0
        }

        onWorldLoad { if (resetOnWorldLoad) arrowCount = 0  }
    }

    fun onDragonSpawn() {
        arrowCount = 0
    }

    override fun onKeybind() {
        if (mc.currentScreen != null || !enabled) return
        arrowCount = 0
    }
}
