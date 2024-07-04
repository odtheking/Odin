package com.github.stivais.ui.renderer

import org.apache.commons.io.IOUtils
import java.io.FileNotFoundException
import java.nio.ByteBuffer
import java.nio.ByteOrder

class Image(
    val resourcePath: String,
    val type: Type = Type.RASTER
) {
    var width = 0f
    var height = 0f


    // im not sure if its required to close the stream
    val stream = this::class.java.getResourceAsStream(resourcePath) ?: throw FileNotFoundException(resourcePath)

    // todo implement
    enum class Type {
        RASTER,
        VECTOR
    }

    override fun hashCode() = resourcePath.hashCode()

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is Image) return false
        return resourcePath == other.resourcePath
    }

    fun buffer(): ByteBuffer {
        val bytes =  IOUtils.toByteArray(stream)
        return ByteBuffer.allocateDirect(bytes.size).order(ByteOrder.nativeOrder()).put(bytes).also { it.flip() }
    }
}