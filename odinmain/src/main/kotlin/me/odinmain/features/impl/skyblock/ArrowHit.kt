package me.odinmain.features.impl.skyblock

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.HudSetting
import me.odinmain.features.settings.impl.NumberSetting
import me.odinmain.features.settings.impl.StringSetting
import me.odinmain.font.OdinFont
import me.odinmain.ui.hud.HudElement
import me.odinmain.utils.clock.Clock
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.getTextWidth
import me.odinmain.utils.render.text
import net.minecraft.network.play.server.S29PacketSoundEffect

object ArrowHit : Module(
    name = "Arrow hit",
    category = Category.SKYBLOCK,
    description = "Counts how many arrows you hit in certain time periods.",
) {
    private val resetOnNumber: Boolean by BooleanSetting("Reset on number", false, description = "Reset the arrow count after a certain number of arrows")
    private val resetCount: String by StringSetting("Reset count", 999999.toString(), 16).withDependency { resetOnNumber }
    private val resetOnTime: Boolean by BooleanSetting("Reset on time", true, description = "Reset the arrow count after a certain amount of time")
    private val resetCountClock: Long by NumberSetting("Reset count clock", 128L, 1f, 10000f, 1f, description = "How many seconds until the arrow count resets").withDependency { resetOnTime}
    private val resetOnWorldLoad by BooleanSetting("Reset on world load", true, description = "Reset the arrow count when you join a world")

    private val resetArrowClock = Clock(resetCountClock * 1000L)
    private var arrowCount = 0

    private val hud: HudElement by HudSetting("Display", 10f, 10f, 2f, false) {
        if (it) {
            text("17.8", 1f, 9f, Color.WHITE,12f, OdinFont.REGULAR)
            getTextWidth("17.8", 12f) to 12f
        } else {
            text("$arrowCount", 1f, 9f, Color.WHITE,12f, OdinFont.REGULAR)
            getTextWidth("$arrowCount", 12f) to 12f
        }
    }

    init {
        onPacket(S29PacketSoundEffect::class.java) {
            if (it.soundName != "random.successful_hit") return@onPacket
            arrowCount += 1
            if (arrowCount >= (resetCount.toIntOrNull() ?: 9999) && resetOnNumber) arrowCount = 0
            if(resetArrowClock.hasTimePassed() && resetOnTime) arrowCount = 0
        }

        onWorldLoad { if (resetOnWorldLoad) arrowCount = 0  }
    }

    override fun onKeybind() {
        if (mc.currentScreen != null || !enabled) return
        arrowCount = 0
    }
}