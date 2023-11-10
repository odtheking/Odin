package me.odinmain.features.impl.skyblock

import cc.polyfrost.oneconfig.renderer.font.Fonts
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.HudSetting
import me.odinmain.ui.hud.HudElement
import me.odinmain.utils.render.gui.nvg.getTextWidth
import me.odinmain.utils.render.gui.nvg.textWithControlCodes
import net.minecraft.network.play.server.S29PacketSoundEffect

object ArrowHit : Module(
    name = "Arrow hit",
    category = Category.SKYBLOCK,
    description = "Counts how many arrows you hit in certain time periods.",
    tag = TagType.NEW
) {

    private val hud: HudElement by HudSetting("Display", 10f, 10f, 2f, false) {
        if (it) { // example
            textWithControlCodes("17.8", 1f, 9f, 16f, Fonts.REGULAR)
            getTextWidth("17.8", 16f, Fonts.REGULAR ) to 16f
        } else {
            textWithControlCodes("$arrowCount", 1f, 9f, 16f, Fonts.REGULAR)
            getTextWidth("$arrowCount", 16f, Fonts.REGULAR ) to 16f
        }
    }

    private var arrowCount = 0

    init {
        onPacket(S29PacketSoundEffect::class.java) {
            if (it.soundName != "random.successful_hit") return@onPacket
            arrowCount += 1
        }
    }

    override fun onKeybind() {
        if (mc.currentScreen != null || !enabled) return
        arrowCount = 0
    }
}