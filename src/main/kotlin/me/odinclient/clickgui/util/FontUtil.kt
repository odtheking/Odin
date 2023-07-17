package me.odinclient.clickgui.util

import cc.polyfrost.oneconfig.renderer.font.Font
import cc.polyfrost.oneconfig.renderer.font.Fonts
import cc.polyfrost.oneconfig.utils.dsl.VG
import cc.polyfrost.oneconfig.utils.dsl.drawText
import cc.polyfrost.oneconfig.utils.dsl.getTextWidth
import java.util.*

object FontUtil {

    fun VG.drawCustomText(string: String, x: Number, y: Number, size: Number = 16f, font: Font = Fonts.REGULAR, color: Int = ColorUtil.textColor) {
        this.drawText(string, x.toFloat() * 2, y.toFloat() * 2, color, size.toFloat(), font)
    }

    fun VG.drawCustomCenteredText(string: String, x: Number, y: Number, size: Number = 16f, font: Font = Fonts.REGULAR, color: Int = ColorUtil.textColor) {
        val textWidth = (x.toFloat() - this.getTextWidth(string, size, Fonts.MEDIUM) / 4f) * 2f
        val textHeight = (y.toFloat() - size.toFloat() / 2) * 2f
        this.drawText(string, textWidth, textHeight, color, size, font)
    }

    fun getStringWidth(vg: VG, string: String, size: Float = 16f, font: Font = Fonts.REGULAR): Float {
        return vg.getTextWidth(string, size, font) / 4f
    }

    fun String.capitalizeOnlyFirst(): String {
        return this.substring(0, 1).uppercase(Locale.getDefault()) + this.substring(1, this.length).lowercase()
    }
}