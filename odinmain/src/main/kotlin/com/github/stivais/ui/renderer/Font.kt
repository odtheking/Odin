package com.github.stivais.ui.renderer

import org.apache.commons.io.IOUtils
import java.io.FileNotFoundException
import java.nio.ByteBuffer
import java.nio.ByteOrder

class Font(val name: String, val resourcePath: String) {

    val buffer: ByteBuffer

    init {
        val stream = this::class.java.getResourceAsStream(resourcePath) ?: throw FileNotFoundException(resourcePath)
        val bytes =  IOUtils.toByteArray(stream)
        stream.close()
        buffer = ByteBuffer.allocateDirect(bytes.size).order(ByteOrder.nativeOrder()).put(bytes)
        buffer.flip()
    }

    override fun hashCode(): Int {
        return resourcePath.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (other !is Font) return false
        return other.resourcePath == resourcePath
    }
}