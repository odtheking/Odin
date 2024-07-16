package me.odinmain.lwjgl

import org.lwjgl.opengl.GLContext
import org.lwjgl.system.FunctionProvider
import java.lang.reflect.Method
import java.nio.ByteBuffer

class LWJGLFunctionProvider : FunctionProvider {
    private var mGetfunctionaddress: Method? = null

    init {
        try {
            mGetfunctionaddress = GLContext::class.java.getDeclaredMethod("getFunctionAddress", String::class.java).let { 
                it.isAccessible = true
                it
            }
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    override fun getFunctionAddress(functionName: CharSequence): Long {
        try {
            return mGetfunctionaddress!!.invoke(null, functionName.toString()) as Long
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    override fun getFunctionAddress(byteBuffer: ByteBuffer): Long {
        throw UnsupportedOperationException()
    }
}