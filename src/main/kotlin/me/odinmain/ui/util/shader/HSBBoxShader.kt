package me.odinmain.ui.util.shader

import me.odinmain.utils.render.Color
import net.minecraft.client.gui.Gui

object HSBBoxShader: Shader("/shaders/source/rectangle.vsh", "/shaders/source/hsbbox.fsh") {

    fun drawHSBBox(x: Float, y: Float, width: Float, height: Float, color: Color) {
        if (!usable) return

        bind()

        getFloat2Uniform("u_rectCenter").setValue(x + (width / 2), y + (height / 2))
        getFloat2Uniform("u_rectSize").setValue(width, height)
        getFloat4Uniform("u_colorRect").setValue(color.r / 255f, color.g / 255f, color.b / 255f, color.alpha)

        Gui.drawRect(x.toInt(), y.toInt(), (x + width).toInt(), (y + height).toInt(), color.rgba)

        unbind()
    }
}