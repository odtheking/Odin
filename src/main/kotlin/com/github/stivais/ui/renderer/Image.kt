package com.github.stivais.ui.renderer

import me.odinmain.utils.setupConnection
import org.apache.commons.io.IOUtils
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.file.Files

class Image(
    val resourcePath: String,
    val type: Type = getType(resourcePath)
) {
    var width = 0f
    var height = 0f

    val stream: InputStream

    init {

        val trimmedPath = resourcePath.trim()

//        val cachedBuffer = cache[resourcePath]
        stream = if (trimmedPath.startsWith("http")) {
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
        val bytes: ByteArray
        stream.use { bytes = IOUtils.toByteArray(it) }
        val buffer = ByteBuffer.allocateDirect(bytes.size).order(ByteOrder.nativeOrder()).put(bytes).also { it.flip() }
        return buffer
    }

    companion object {
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