package me.odinmain.font

import me.odinmain.utils.render.Color

object FontRenderer {

    private var fontRenderer = GlyphPageFontRenderer.create("/Inter-Regular.ttf", 18f)

    fun drawString(text: String, x: Number, y: Number, color: Color, shadow: Boolean) {
        fontRenderer.drawString(text, x.toFloat(), y.toFloat(), color, shadow)
    }

    fun getWidth(text: String): Int {
        return fontRenderer.getStringWidth(text)
    }
}