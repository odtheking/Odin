package com.github.stivais.ui.renderer

import org.lwjgl.nanovg.NSVGImage
import org.lwjgl.nanovg.NanoSVG
import java.io.FileNotFoundException

class SVG(
    val resourcePath: String,
) {

    val svgImage: NSVGImage = NanoSVG.nsvgParseFromFile(resourcePath, "px", 96f) ?: throw FileNotFoundException(resourcePath)

    override fun hashCode() = resourcePath.hashCode()

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is SVG) return false
        return resourcePath == other.resourcePath
    }

}