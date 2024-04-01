package me.odinmain.ui.util.shader

import me.odinmain.OdinMain.mc
import org.lwjgl.opengl.GL20

object GlowShader : FramebufferShader("glow.fsh") {
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
        GL20.glUniform3f(getUniform("color"), red, green, blue)
        GL20.glUniform1f(getUniform("divider"), 140f)
        GL20.glUniform1f(getUniform("radius"), radius)
        GL20.glUniform1f(getUniform("maxSample"), 10f)
    }
}