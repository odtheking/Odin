package me.odinmain.font

import me.odinmain.utils.render.Color
import net.minecraft.util.ResourceLocation

object FontRenderer {

    private var fontRenderer = GlyphPageFontRenderer.create(ResourceLocation("odinmain", "font/Inter-Regular.ttf"), 18f)

    fun drawString(text: String, x: Float, y: Float, color: Color, shadow: Boolean) {
        fontRenderer.drawString(text, x, y, color, shadow)
    }

}