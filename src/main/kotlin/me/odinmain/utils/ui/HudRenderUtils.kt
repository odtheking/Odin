package me.odinmain.utils.ui

import me.odinmain.OdinMain.mc
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.RenderUtils

fun drawStringWidth(text: String, x: Number, y: Number, color: Color, shadow: Boolean = true): Float {
    RenderUtils.drawText(text, x.toFloat(), y.toFloat(), color, shadow)
    return getTextWidth(text).toFloat()
}

fun getTextWidth(text: String) = mc.fontRendererObj.getStringWidth(text)

fun getMCTextHeight() = mc.fontRendererObj.FONT_HEIGHT


