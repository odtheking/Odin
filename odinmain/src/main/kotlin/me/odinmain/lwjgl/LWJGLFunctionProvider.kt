package me.odinmain.lwjgl

import org.lwjgl.opengl.GLContext
import org.lwjgl.system.FunctionProvider
import java.lang.reflect.Method
import java.nio.ByteBuffer

class LWJGLFunctionProvider : FunctionProvider {

    private var getFunctionAddress: Method? = null

    init {
        try {
            getFunctionAddress = GLContext::class.java.getDeclaredMethod("getFunctionAddress", String::class.java)
            getFunctionAddress?.isAccessible = true
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    override fun getFunctionAddress(functionName: CharSequence): Long {
        try {
            return getFunctionAddress!!.invoke(null, functionName.toString()) as Long
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    override fun getFunctionAddress(byteBuffer: ByteBuffer?): Long {
        throw UnsupportedOperationException()
    }

}