package com.github.stivais.ui.renderer

import me.odinmain.font.OdinFont
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.TextPos
import me.odinmain.utils.render.roundedRectangle

// op renderer odin client
// maybe isn't as fair comparsion as i cba to make it use new colors since im gonig to replace it soon
object CookedRenderer : Renderer {

    override fun beginFrame() {
    }

    override fun endFrame() {
    }

    override fun rect(x: Float, y: Float, w: Float, h: Float, color: Int) {
        roundedRectangle(x, y, w, h, Color(color))
    }

    override fun rect(x: Float, y: Float, w: Float, h: Float, color: Int, tl: Float, bl: Float, br: Float, tr: Float) {
        val stupidColor = Color(color)
        roundedRectangle(x, y, w, h, stupidColor, stupidColor, stupidColor, 1f, tl, tr, bl, br, 1f)
    }

    override fun hollowRect(
        x: Float,
        y: Float,
        w: Float,
        h: Float,
        thickness: Float,
        color: Int,
        tl: Float,
        bl: Float,
        br: Float,
        tr: Float
    ) {
        val stupidColor = Color(color)
        roundedRectangle(x, y, w, h, Color.TRANSPARENT, stupidColor, Color.TRANSPARENT, 1f, tl, tr, bl, br, 1f)
    }

    override fun text(text: String, x: Float, y: Float, size: Float, color: Int) {
        OdinFont.text(text, x, y, Color(color), size, verticalAlign = TextPos.Top
        )
    }
}