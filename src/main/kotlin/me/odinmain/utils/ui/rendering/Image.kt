package me.odinmain.utils.ui.rendering

import kotlinx.coroutines.runBlocking
import me.odinmain.utils.network.WebUtils.getInputStream
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.file.Files

class Image(
    val identifier: String,
    val isSVG: Boolean = identifier.endsWith(".svg", true),
    var stream: InputStream = getStream(identifier),
    private var buffer: ByteBuffer? = null
) {
    constructor(inputStream: InputStream, identifier: String) : this(identifier, false, inputStream) { stream = inputStream }

    fun buffer(): ByteBuffer {
        if (buffer == null) {
            val bytes = stream.readBytes()
            buffer = ByteBuffer.allocateDirect(bytes.size).order(ByteOrder.nativeOrder()).put(bytes).also { it.flip() }
            stream.close()
        }
        return buffer ?: throw IllegalStateException("Image has no data")
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Image) return false
        return identifier == other.identifier
    }

    override fun hashCode(): Int {
        return identifier.hashCode()
    }

    companion object {

        private fun getStream(path: String): InputStream {
            val trimmedPath = path.trim()
            return if (trimmedPath.startsWith("http")) runBlocking { getInputStream(trimmedPath).getOrThrow() } // this is really weird but the old one behaved the same...
            else {
                val file = File(trimmedPath)
                if (file.exists() && file.isFile) Files.newInputStream(file.toPath())
                else this::class.java.getResourceAsStream(trimmedPath) ?: throw FileNotFoundException(trimmedPath)
            }
        }
    }
}