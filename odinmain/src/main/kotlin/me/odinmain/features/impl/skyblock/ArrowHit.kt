package me.odinmain.features.impl.skyblock

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.HudSetting
import me.odinmain.features.settings.impl.NumberSetting
import me.odinmain.features.settings.impl.StringSetting
import me.odinmain.ui.hud.HudElement
import me.odinmain.utils.clock.Clock
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.gui.Fonts
import me.odinmain.utils.render.gui.getTextWidth
import me.odinmain.utils.render.gui.text
import net.minecraft.network.play.server.S29PacketSoundEffect

object ArrowHit : Module(
    name = "Arrow hit",
    category = Category.SKYBLOCK,
    description = "Counts how many arrows you hit in certain time periods",
) {
    private val resetCount: String by StringSetting("Reset count", 999999.toString(), 32)
    private val resetCountClock: Long by NumberSetting("Reset count clock", 128L, 1f, 10000f, 1f, description = "How many seconds until the arrow count resets")

    private val resetArrowClock = Clock(resetCountClock * 1000L)
    private var arrowCount = 0

    private val hud: HudElement by HudSetting("Display", 10f, 10f, 2f, false) {
        if (it) {
            text("17.8", 1f, 9f, Color.WHITE,16f, Fonts.REGULAR)
            getTextWidth("17.8", 16f, Fonts.REGULAR ) to 16f
        } else {
            text("$arrowCount", 1f, 9f, Color.WHITE,16f, Fonts.REGULAR)
            getTextWidth("$arrowCount", 16f, Fonts.REGULAR ) to 16f
        }
    }

    init {
        onPacket(S29PacketSoundEffect::class.java) {
            if (it.soundName != "random.successful_hit") return@onPacket
            arrowCount += 1
            if (arrowCount >= resetCount.toInt()) arrowCount = 0
            if(resetArrowClock.hasTimePassed()) arrowCount = 0
            resetArrowClock.update()

        }
    }

    override fun onKeybind() {
        if (mc.currentScreen != null || !enabled) return
        arrowCount = 0
    }
}