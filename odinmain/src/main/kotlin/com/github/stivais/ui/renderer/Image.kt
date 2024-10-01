package com.github.stivais.ui.renderer

import me.odinmain.utils.setupConnection
import org.apache.commons.io.IOUtils
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.file.Files
import java.util.LinkedHashMap

class Image(
    val resourcePath: String,
    val type: Type = getType(resourcePath)
) {
    var width = 0f
    var height = 0f

    val trimmedPath = resourcePath.trim()
    val stream: InputStream

    init {
        val cachedBuffer = cache[resourcePath]
        stream = if (cachedBuffer != null) {
            cachedBuffer.asInputStream()
        } else {
            if (trimmedPath.startsWith("http")) {
                setupConnection(trimmedPath, "Odin", 5000, true)
            } else {
                val file = File(trimmedPath)
                if (file.exists() && file.isFile) {
                    Files.newInputStream(file.toPath())
                } else {
                    this::class.java.getResourceAsStream(trimmedPath) ?: throw FileNotFoundException(trimmedPath)
                }
            }
        }
    }

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
        val cachedBuffer = cache[resourcePath]
        if (cachedBuffer != null) return cachedBuffer

        val bytes: ByteArray

        stream.use {
            bytes = IOUtils.toByteArray(it)
        }

        val buffer = ByteBuffer.allocateDirect(bytes.size).order(ByteOrder.nativeOrder()).put(bytes).also { it.flip() }
        cache[resourcePath] = buffer
        return buffer
    }

    companion object {
        private const val MAX_CACHE_SIZE = 20

        private val cache = object : LinkedHashMap<String, ByteBuffer>(MAX_CACHE_SIZE, 0.75f, true) {
            override fun removeEldestEntry(eldest: Map.Entry<String, ByteBuffer>): Boolean {
                return size > MAX_CACHE_SIZE
            }
        }

        fun getType(path: String): Type {
            return if (path.substringAfterLast('.') == "svg") Type.VECTOR else Type.RASTER
        }
    }
}

fun ByteBuffer.asInputStream(): InputStream {
    val byteArray = ByteArray(this.remaining())
    this.get(byteArray)
    return byteArray.inputStream()
}