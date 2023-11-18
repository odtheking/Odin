/*
 * Copyright (c) 2018 superblaubeere27
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package me.odinmain.font

import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.texture.DynamicTexture
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.awt.Font
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.font.FontRenderContext
import java.awt.geom.AffineTransform
import java.awt.image.BufferedImage
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.sqrt

class GlyphPage(private val font: Font, val isAntiAliasingEnabled: Boolean, val isFractionalMetricsEnabled: Boolean) {
    private var imgSize = 0
    var maxFontHeight = -1
        private set
    private val glyphCharacterMap = HashMap<Char, Glyph>()
    private var bufferedImage: BufferedImage? = null
    private var loadedTexture: DynamicTexture? = null
    fun generateGlyphPage(chars: CharArray) {
        // Calculate glyphPageSize
        var maxWidth = -1.0
        var maxHeight = -1.0
        val affineTransform = AffineTransform()
        val fontRenderContext = FontRenderContext(
            affineTransform,
            isAntiAliasingEnabled,
            isFractionalMetricsEnabled
        )
        for (ch in chars) {
            val bounds = font.getStringBounds(ch.toString(), fontRenderContext)
            if (maxWidth < bounds.width) maxWidth = bounds.width
            if (maxHeight < bounds.height) maxHeight = bounds.height
        }

        // Leave some additional space
        maxWidth += 2.0
        maxHeight += 2.0
        imgSize = ceil(
            max(
                ceil(sqrt(maxWidth * maxWidth * chars.size) / maxWidth),
                ceil(sqrt(maxHeight * maxHeight * chars.size) / maxHeight)
            ) * max(maxWidth, maxHeight)
        )
            .toInt() + 1
        bufferedImage = BufferedImage(imgSize, imgSize, BufferedImage.TYPE_INT_ARGB)
        val g = bufferedImage!!.graphics as Graphics2D
        g.font = font
        // Set Color to Transparent
        g.color = Color(255, 255, 255, 0)
        // Set the image background to transparent
        g.fillRect(0, 0, imgSize, imgSize)
        g.color = Color.white
        g.setRenderingHint(
            RenderingHints.KEY_FRACTIONALMETRICS,
            if (isFractionalMetricsEnabled) RenderingHints.VALUE_FRACTIONALMETRICS_ON else RenderingHints.VALUE_FRACTIONALMETRICS_OFF
        )
        g.setRenderingHint(
            RenderingHints.KEY_ANTIALIASING,
            if (isAntiAliasingEnabled) RenderingHints.VALUE_ANTIALIAS_OFF else RenderingHints.VALUE_ANTIALIAS_ON
        )
        g.setRenderingHint(
            RenderingHints.KEY_TEXT_ANTIALIASING,
            if (isAntiAliasingEnabled) RenderingHints.VALUE_TEXT_ANTIALIAS_ON else RenderingHints.VALUE_TEXT_ANTIALIAS_OFF
        )
        val fontMetrics = g.fontMetrics
        var currentCharHeight = 0
        var posX = 0
        var posY = 1
        for (ch in chars) {
            val glyph = Glyph()
            val bounds = fontMetrics.getStringBounds(ch.toString(), g)
            glyph.width = bounds.getBounds().width + 8 // Leave some additional space
            glyph.height = bounds.getBounds().height
            check(posY + glyph.height < imgSize) { "Not all characters will fit" }
            if (posX + glyph.width >= imgSize) {
                posX = 0
                posY += currentCharHeight
                currentCharHeight = 0
            }
            glyph.x = posX
            glyph.y = posY
            if (glyph.height > maxFontHeight) maxFontHeight = glyph.height
            if (glyph.height > currentCharHeight) currentCharHeight = glyph.height
            g.drawString(ch.toString(), posX + 2, posY + fontMetrics.ascent)
            posX += glyph.width
            glyphCharacterMap[ch] = glyph
        }
    }

    fun setupTexture() {
        loadedTexture = DynamicTexture(bufferedImage)
    }

    fun bindTexture() {
        GlStateManager.bindTexture(loadedTexture!!.getGlTextureId())
    }

    fun unbindTexture() {
        GlStateManager.bindTexture(0)
    }

    fun drawChar(ch: Char, x: Float, y: Float): Float {
        val glyph = glyphCharacterMap[ch] ?: throw IllegalArgumentException("'$ch' wasn't found")
        val pageX = glyph.x / imgSize.toFloat()
        val pageY = glyph.y / imgSize.toFloat()
        val pageWidth = glyph.width / imgSize.toFloat()
        val pageHeight = glyph.height / imgSize.toFloat()
        val width = glyph.width.toFloat()
        val height = glyph.height.toFloat()
        GL11.glBegin(GL11.GL_TRIANGLES)
        GL11.glTexCoord2f(pageX + pageWidth, pageY)
        GL11.glVertex2f(x + width, y)
        GL11.glTexCoord2f(pageX, pageY)
        GL11.glVertex2f(x, y)
        GL11.glTexCoord2f(pageX, pageY + pageHeight)
        GL11.glVertex2f(x, y + height)
        GL11.glTexCoord2f(pageX, pageY + pageHeight)
        GL11.glVertex2f(x, y + height)
        GL11.glTexCoord2f(pageX + pageWidth, pageY + pageHeight)
        GL11.glVertex2f(x + width, y + height)
        GL11.glTexCoord2f(pageX + pageWidth, pageY)
        GL11.glVertex2f(x + width, y)
        GL11.glEnd()
        return width - 8
    }

    fun getWidth(ch: Char): Float {
        return glyphCharacterMap[ch]!!.width.toFloat()
    }

    internal class Glyph {
        var x = 0
        var y = 0
        var width = 0
        var height = 0

        constructor(x: Int, y: Int, width: Int, height: Int) {
            this.x = x
            this.y = y
            this.width = width
            this.height = height
        }

        constructor()
    }
}