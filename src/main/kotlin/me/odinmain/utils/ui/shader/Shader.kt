package me.odinmain.utils.ui.shader

import net.minecraft.client.renderer.OpenGlHelper
import org.apache.commons.io.IOUtils
import org.lwjgl.opengl.ARBShaderObjects
import org.lwjgl.opengl.ARBShaderObjects.glShaderSourceARB
import org.lwjgl.opengl.ARBShaderObjects.glValidateProgramARB
import org.lwjgl.opengl.GL20

abstract class Shader(vertexShader: String, fragmentShader: String) {
    private var programId: Int = OpenGlHelper.glCreateProgram()
    private var vertShader: Int = OpenGlHelper.glCreateShader(GL20.GL_VERTEX_SHADER)
    private var fragShader: Int = OpenGlHelper.glCreateShader(GL20.GL_FRAGMENT_SHADER)
    private var uniformsMap: HashMap<String, Int> = HashMap()

    var usable = false
        private set

    init {
        val vertexStream = javaClass.getResourceAsStream(vertexShader) ?: throw IllegalArgumentException("Vertex shader not found: $vertexShader")
        val vertexSource = IOUtils.toString(vertexStream)
        IOUtils.closeQuietly(vertexStream)

        val fragmentStream = javaClass.getResourceAsStream(fragmentShader) ?: throw IllegalArgumentException("Fragment shader not found: $fragmentShader")
        val fragmentSource = IOUtils.toString(fragmentStream)
        IOUtils.closeQuietly(fragmentStream)

        createShader(vertexSource, fragmentSource)
    }

    private fun createShader(vertexShader: String, fragmentShader: String) {
        for ((shader, source) in listOf(vertShader to vertexShader, fragShader to fragmentShader)) {
            if (OpenGlHelper.openGL21) GL20.glShaderSource(shader, source) else glShaderSourceARB(shader, source)
            OpenGlHelper.glCompileShader(shader)

            if (OpenGlHelper.glGetShaderi(shader, GL20.GL_COMPILE_STATUS) != 1) {
                println(OpenGlHelper.glGetShaderInfoLog(shader, 32768))
                return
            }

            OpenGlHelper.glAttachShader(programId, shader)
        }

        OpenGlHelper.glLinkProgram(programId)

        if (OpenGlHelper.openGL21) {
            GL20.glDetachShader(programId, vertShader)
            GL20.glDetachShader(programId, fragShader)
            GL20.glDeleteShader(vertShader)
            GL20.glDeleteShader(fragShader)
        } else {
            ARBShaderObjects.glDetachObjectARB(programId, vertShader)
            ARBShaderObjects.glDetachObjectARB(programId, fragShader)
            ARBShaderObjects.glDeleteObjectARB(vertShader)
            ARBShaderObjects.glDeleteObjectARB(fragShader)
        }

        if (OpenGlHelper.glGetProgrami(programId, GL20.GL_LINK_STATUS) != 1) {
            println(OpenGlHelper.glGetProgramInfoLog(programId, 32768))
            return
        }

        if (OpenGlHelper.openGL21) GL20.glValidateProgram(programId) else glValidateProgramARB(programId)

        if (OpenGlHelper.glGetProgrami(programId, GL20.GL_VALIDATE_STATUS) != 1) {
            println(OpenGlHelper.glGetProgramInfoLog(programId, 32768))
            return
        }

        usable = true
    }

    fun bind() {
        OpenGlHelper.glUseProgram(programId)
    }

    fun unbind() {
        OpenGlHelper.glUseProgram(0)
    }

    private fun getUniformLocation(uniformName: String): Int {
        return uniformsMap.getOrPut(uniformName) {
            val location = OpenGlHelper.glGetUniformLocation(programId, uniformName)
            if (location == -1) println("Uniform $uniformName not found in shader")
            location
        }
    }

    fun getFloatUniform(name: String): FloatUniform =
        FloatUniform(getUniformLocation(name))

    fun getFloat2Uniform(name: String): Float2Uniform =
        Float2Uniform(getUniformLocation(name))

    fun getFloat4Uniform(name: String): Float4Uniform =
        Float4Uniform(getUniformLocation(name))
}