package me.odinmain.ui.util.shader

import me.odinmain.OdinMain.mc
import me.odinmain.utils.render.Color
import org.lwjgl.opengl.GL20
import java.awt.Color.*

object OutlineShader : FramebufferShader("outline.fsh") {
    override fun setupUniforms() {
        setupUniform("texture")
        setupUniform("texelSize")
        setupUniform("color")
        setupUniform("divider")
        setupUniform("radius")
        setupUniform("maxSample")
    }

    override fun updateUniforms() {
        GL20.glUniform1i(getUniform("texture"), 0)
        GL20.glUniform2f(
            getUniform("texelSize"),
            1f / mc.displayWidth * (radius * quality),
            1f / mc.displayHeight * (radius * quality)
        )
        updateColor(this.color)
        updateThickness(radius)
    }

    private fun updateColor(color: Color) {
        GL20.glUniform4f(getUniform("color"), color.r / 255f, color.g / 255f, color.b / 255f, color.alpha)
    }

    private fun updateThickness(thickness: Float) {
        GL20.glUniform1f(getUniform("radius"), thickness)
    }
}