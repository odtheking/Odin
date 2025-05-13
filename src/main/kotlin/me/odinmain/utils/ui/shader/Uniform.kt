package me.odinmain.utils.ui.shader

import net.minecraft.client.renderer.OpenGlHelper
import org.lwjgl.opengl.ARBShaderObjects
import org.lwjgl.opengl.GL20

class FloatUniform(private val location: Int) {
    fun setValue(value: Float) {
        if (OpenGlHelper.openGL21) GL20.glUniform1f(location, value)
        else ARBShaderObjects.glUniform1fARB(location, value)
    }
}

class Float2Uniform(private val location: Int) {
    fun setValue(x: Float, y: Float) {
        if (OpenGlHelper.openGL21) GL20.glUniform2f(location, x, y)
        else ARBShaderObjects.glUniform2fARB(location, x, y)
    }
}

class Float4Uniform(private val location: Int) {
    fun setValue(x: Float, y: Float, z: Float, w: Float) {
        if (OpenGlHelper.openGL21) GL20.glUniform4f(location, x, y, z, w)
        else ARBShaderObjects.glUniform4fARB(location, x, y, z, w)
    }
}