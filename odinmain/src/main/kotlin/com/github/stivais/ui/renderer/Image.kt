package com.github.stivais.ui.renderer

import org.apache.commons.io.IOUtils
import java.io.FileNotFoundException
import java.nio.ByteBuffer
import java.nio.ByteOrder

class Image(
    val resourcePath: String,
//    val type: Type
) {
    val buffer: ByteBuffer

    init {
        val stream = this::class.java.getResourceAsStream(resourcePath) ?: throw FileNotFoundException(resourcePath)
        val bytes =  IOUtils.toByteArray(stream)
        stream.close()
        buffer = ByteBuffer.allocateDirect(bytes.size).order(ByteOrder.nativeOrder()).put(bytes)
        buffer.flip()
    }

//    enum class Type {
//        RASTER,
//        VECTOR // todo implement
//    }

    override fun hashCode() = resourcePath.hashCode()

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is Image) return false
        return resourcePath == other.resourcePath
    }
}