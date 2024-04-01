package me.odinmain.ui.util.shader

import me.odinmain.OdinMain.mc
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
        GL20.glUniform4f(getUniform("color"), red, green, blue, alpha)
        GL20.glUniform1f(getUniform("radius"), radius)
    }
}