package me.odinmain.ui.util.shader

import me.odinmain.OdinMain.mc
import me.odinmain.utils.render.Color
import org.lwjgl.opengl.GL20

object GlowShader : FramebufferShader("glow.fsh") {
    private var glowIntensity = 2f

    override fun setupUniforms() {
        setupUniform("texture")
        setupUniform("texelSize")
        setupUniform("color")
        setupUniform("radius")
        setupUniform("glow_intensity")
    }

    override fun updateUniforms() {
        GL20.glUniform1i(getUniform("texture"), 0)
        GL20.glUniform2f(
            getUniform("texelSize"),
            1f / mc.displayWidth * radius,
            1f / mc.displayHeight * radius
        )
        GL20.glUniform3f(getUniform("color"), red, green, blue)
        GL20.glUniform1f(getUniform("radius"), radius)
        GL20.glUniform1f(getUniform("glow_intensity"), glowIntensity)
    }

    fun endDraw(color: Color, radius: Float, glowInt: Float) {
        glowIntensity = glowInt
        stopDraw(color, radius, 1f)
    }
}