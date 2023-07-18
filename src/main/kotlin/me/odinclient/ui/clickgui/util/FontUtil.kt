package me.odinclient.ui.clickgui.util

import cc.polyfrost.oneconfig.renderer.font.Font
import cc.polyfrost.oneconfig.renderer.font.Fonts
import cc.polyfrost.oneconfig.utils.dsl.VG
import cc.polyfrost.oneconfig.utils.dsl.drawText
import cc.polyfrost.oneconfig.utils.dsl.getTextWidth
import java.util.*

object FontUtil {

    fun VG.drawCustomCenteredText(string: String, x: Float, y: Float, size: Float = 16f, font: Font = Fonts.REGULAR, color: Int = ColorUtil.textColor) {
        val textWidth = (x - this.getTextWidth(string, size, Fonts.MEDIUM) / 2f)
        drawText(string, textWidth, y, color, size, font)
    }
    // dont use tihs
    fun getStringWidth(vg: VG, string: String, size: Float = 16f, font: Font = Fonts.REGULAR): Float {
        return vg.getTextWidth(string, size, font) / 2f
    }

    fun String.capitalizeOnlyFirst(): String {
        return this.substring(0, 1).uppercase(Locale.getDefault()) + this.substring(1, this.length).lowercase()
    }
}