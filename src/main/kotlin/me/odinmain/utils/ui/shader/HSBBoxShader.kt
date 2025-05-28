package me.odinmain.utils.ui.shader

import me.odinmain.utils.render.Color
import me.odinmain.utils.ui.Colors
import net.minecraft.client.gui.Gui

object HSBBoxShader: Shader("/shaders/rectangle.vsh", "/shaders/hsbbox.fsh") {

    private var x = 0f
    private var y = 0f
    private var width = 0f
    private var height = 0f
    private var color = Colors.WHITE

    fun drawHSBBox(x: Float, y: Float, width: Float, height: Float, color: Color) {
        if (!usable) return

        this.x = x
        this.y = y
        this.width = width
        this.height = height
        this.color = color

        startShader()

        Gui.drawRect(x.toInt(), y.toInt(), (x + width).toInt(), (y + height).toInt(), color.rgba)

        stopShader()
    }

    override fun setupUniforms() {
        setupUniform("u_rectCenter")
        setupUniform("u_rectSize")
        setupUniform("u_colorRect")
    }

    override fun updateUniforms() {
        getFloat2Uniform("u_rectCenter").setValue(x + (width / 2), y + (height / 2))
        getFloat2Uniform("u_rectSize").setValue(width, height)
        getFloat4Uniform("u_colorRect").setValue(color.red / 255f, color.green / 255f, color.blue / 255f, color.alphaFloat)
    }
}