package me.odinmain.utils.ui.rendering

import java.io.FileNotFoundException
import java.nio.ByteBuffer
import java.nio.ByteOrder

class Font(val name: String, val resourcePath: String) {

    fun buffer(): ByteBuffer {
        val stream = this::class.java.getResourceAsStream(resourcePath) ?: throw FileNotFoundException(resourcePath)
        val bytes = stream.readBytes()
        stream.close()
        val buffer = ByteBuffer.allocateDirect(bytes.size).order(ByteOrder.nativeOrder()).put(bytes)
        buffer.flip()
        return buffer
    }

    override fun hashCode(): Int {
        return resourcePath.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (other !is Font) return false
        return other.resourcePath == resourcePath
    }
}