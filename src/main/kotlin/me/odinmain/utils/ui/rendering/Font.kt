package me.odinmain.utils.ui.rendering

import java.io.FileNotFoundException
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

class Font {
    val name: String
    private val resourcePath: String?
    private val cachedBytes: ByteArray?

    constructor(name: String, resourcePath: String) {
        this.name = name
        this.resourcePath = resourcePath
        this.cachedBytes = null
    }

    constructor(name: String, inputStream: InputStream) {
        this.name = name
        this.resourcePath = null
        this.cachedBytes = inputStream.use { it.readBytes() }
    }

    fun buffer(): ByteBuffer {
        val bytes = cachedBytes ?: run {
            val stream = this::class.java.getResourceAsStream(resourcePath!!) ?: throw FileNotFoundException(resourcePath)
            stream.use { it.readBytes() }
        }

        return ByteBuffer.allocateDirect(bytes.size)
            .order(ByteOrder.nativeOrder())
            .put(bytes)
            .flip() as ByteBuffer
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return other is Font && name == other.name
    }
}