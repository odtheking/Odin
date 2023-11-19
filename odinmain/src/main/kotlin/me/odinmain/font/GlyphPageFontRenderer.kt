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

import me.odinmain.OdinMain.mc
import me.odinmain.utils.render.Color
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11
import java.awt.Font
import java.awt.FontFormatException
import java.io.IOException
import kotlin.math.max

class GlyphPageFontRenderer(
    private val regularGlyphPage: GlyphPage,
    private val boldGlyphPage: GlyphPage,
    private val italicGlyphPage: GlyphPage,
    private val boldItalicGlyphPage: GlyphPage
) {
    /**
     * Current X coordinate at which to draw the next character.
     */
    private var posX = 0f

    /**
     * Current Y coordinate at which to draw the next character.
     */
    private var posY = 0f

    /**
     * Array of RGB triplets defining the 16 standard chat colors followed by 16 darker version of the same colors for
     * drop shadows.
     */
    private val colorCode = IntArray(32)

    /**
     * Used to specify new red value for the current color.
     */
    private var red = 0f

    /**
     * Used to specify new blue value for the current color.
     */
    private var blue = 0f

    /**
     * Used to specify new green value for the current color.
     */
    private var green = 0f

    /**
     * Used to speify new alpha value for the current color.
     */
    private var alpha = 0f

    /**
     * Text color of the currently rendering string.
     */
    private var textColor = 0

    /**
     * Set if the "k" style (random) is active in currently rendering string
     */
    private var randomStyle = false

    /**
     * Set if the "l" style (bold) is active in currently rendering string
     */
    private var boldStyle = false

    /**
     * Set if the "o" style (italic) is active in currently rendering string
     */
    private var italicStyle = false

    /**
     * Set if the "n" style (underlined) is active in currently rendering string
     */
    private var underlineStyle = false

    /**
     * Set if the "m" style (strikethrough) is active in currently rendering string
     */
    private var strikethroughStyle = false

    init {
        for (i in 0..31) {
            val j = (i shr 3 and 1) * 85
            var k = (i shr 2 and 1) * 170 + j
            var l = (i shr 1 and 1) * 170 + j
            var i1 = (i and 1) * 170 + j
            if (i == 6) {
                k += 85
            }
            if (i >= 16) {
                k /= 4
                l /= 4
                i1 /= 4
            }
            colorCode[i] = k and 255 shl 16 or (l and 255 shl 8) or (i1 and 255)
        }
    }

    /**
     * Draws the specified string.
     */
    fun drawString(text: String?, x: Float, y: Float, color: Color, dropShadow: Boolean): Int {
        GlStateManager.enableAlpha()
        resetStyles()
        var i: Int
        if (dropShadow) {
            i = renderString(text, x + 1.0f, y + 1.0f, color, true)
            i = max(i.toDouble(), renderString(text, x, y, color, false).toDouble()).toInt()
        } else {
            i = renderString(text, x, y, color, false)
        }
        return i
    }

    /**
     * Render single line string by setting GL color, current (posX,posY), and calling renderStringAtPos()
     */
    private fun renderString(text: String?, x: Float, y: Float, color: Color, dropShadow: Boolean): Int {
        return if (text == null) {
            0
        } else {
            var colorInt = color.rgba
            if (colorInt and -67108864 == 0) {
                colorInt = colorInt or -16777216
            }
            if (dropShadow) {
                colorInt = colorInt and 16579836 shr 2 or (colorInt and -16777216)
            }
            red = (colorInt shr 16 and 255).toFloat() / 255.0f
            blue = (colorInt shr 8 and 255).toFloat() / 255.0f
            green = (colorInt and 255).toFloat() / 255.0f
            alpha = (colorInt shr 24 and 255).toFloat() / 255.0f
            GlStateManager.color(red, blue, green, alpha)
            posX = x * 2.0f
            posY = y * 2.0f
            renderStringAtPos(text, dropShadow)
            posX.toInt()
        }
    }

    /**
     * Render a single line string at the current (posX,posY) and update posX
     */
    private fun renderStringAtPos(text: String, shadow: Boolean) {
        var glyphPage = currentGlyphPage
        GL11.glPushMatrix()
        GL11.glScaled(0.5, 0.5, 0.5)
        GlStateManager.enableBlend()
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        GlStateManager.enableTexture2D()
        glyphPage.bindTexture()
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR)
        var i = 0
        while (i < text.length) {
            val c0 = text[i]
            if (c0.code == 167 && i + 1 < text.length) {
                var i1 = "0123456789abcdefklmnor".indexOf(text.lowercase()[i + 1])
                if (i1 < 16) {
                    randomStyle = false
                    boldStyle = false
                    strikethroughStyle = false
                    underlineStyle = false
                    italicStyle = false
                    if (i1 < 0) {
                        i1 = 15
                    }
                    if (shadow) {
                        i1 += 16
                    }
                    val j1 = colorCode[i1]
                    textColor = j1
                    GlStateManager.color(
                        (j1 shr 16).toFloat() / 255.0f,
                        (j1 shr 8 and 255).toFloat() / 255.0f,
                        (j1 and 255).toFloat() / 255.0f,
                        alpha
                    )
                } else if (i1 == 16) {
                    randomStyle = true
                } else if (i1 == 17) {
                    boldStyle = true
                } else if (i1 == 18) {
                    strikethroughStyle = true
                } else if (i1 == 19) {
                    underlineStyle = true
                } else if (i1 == 20) {
                    italicStyle = true
                } else {
                    randomStyle = false
                    boldStyle = false
                    strikethroughStyle = false
                    underlineStyle = false
                    italicStyle = false
                    GlStateManager.color(red, blue, green, alpha)
                }
                ++i
            } else {
                glyphPage = currentGlyphPage
                glyphPage.bindTexture()
                val f = glyphPage.drawChar(c0, posX, posY)
                doDraw(f, glyphPage)
            }
            ++i
        }
        glyphPage.unbindTexture()
        GL11.glPopMatrix()
    }

    private fun doDraw(f: Float, glyphPage: GlyphPage) {
        if (strikethroughStyle) {
            val tessellator = Tessellator.getInstance()
            val worldrenderer = tessellator.worldRenderer
            GlStateManager.disableTexture2D()
            worldrenderer.begin(7, DefaultVertexFormats.POSITION)
            worldrenderer.pos(posX.toDouble(), (posY + (glyphPage.maxFontHeight / 2).toFloat()).toDouble(), 0.0)
                .endVertex()
            worldrenderer.pos((posX + f).toDouble(), (posY + (glyphPage.maxFontHeight / 2).toFloat()).toDouble(), 0.0)
                .endVertex()
            worldrenderer.pos(
                (posX + f).toDouble(),
                (posY + (glyphPage.maxFontHeight / 2).toFloat() - 1.0f).toDouble(),
                0.0
            ).endVertex()
            worldrenderer.pos(posX.toDouble(), (posY + (glyphPage.maxFontHeight / 2).toFloat() - 1.0f).toDouble(), 0.0)
                .endVertex()
            tessellator.draw()
            GlStateManager.enableTexture2D()
        }
        if (underlineStyle) {
            val tessellator1 = Tessellator.getInstance()
            val worldrenderer1 = tessellator1.worldRenderer
            GlStateManager.disableTexture2D()
            worldrenderer1.begin(7, DefaultVertexFormats.POSITION)
            val l = if (underlineStyle) -1 else 0
            worldrenderer1.pos(
                (posX + l.toFloat()).toDouble(),
                (posY + glyphPage.maxFontHeight.toFloat()).toDouble(),
                0.0
            ).endVertex()
            worldrenderer1.pos((posX + f).toDouble(), (posY + glyphPage.maxFontHeight.toFloat()).toDouble(), 0.0)
                .endVertex()
            worldrenderer1.pos((posX + f).toDouble(), (posY + glyphPage.maxFontHeight.toFloat() - 1.0f).toDouble(), 0.0)
                .endVertex()
            worldrenderer1.pos(
                (posX + l.toFloat()).toDouble(),
                (posY + glyphPage.maxFontHeight.toFloat() - 1.0f).toDouble(),
                0.0
            ).endVertex()
            tessellator1.draw()
            GlStateManager.enableTexture2D()
        }
        posX += f
    }

    private val currentGlyphPage: GlyphPage
        get() = if (boldStyle && italicStyle) boldItalicGlyphPage else if (boldStyle) boldGlyphPage else if (italicStyle) italicGlyphPage else regularGlyphPage

    /**
     * Reset all style flag fields in the class to false; called at the start of string rendering
     */
    private fun resetStyles() {
        randomStyle = false
        boldStyle = false
        italicStyle = false
        underlineStyle = false
        strikethroughStyle = false
    }

    val fontHeight: Int
        get() = regularGlyphPage.maxFontHeight / 2

    fun getStringWidth(text: String?): Int {
        if (text == null) {
            return 0
        }
        var width = 0
        var currentPage: GlyphPage
        val size = text.length
        var on = false
        var i = 0
        while (i < size) {
            var character = text[i]
            if (character == '§') on = true else if (on && character >= '0' && character <= 'r') {
                val colorIndex = "0123456789abcdefklmnor".indexOf(character)
                if (colorIndex < 16) {
                    boldStyle = false
                    italicStyle = false
                } else if (colorIndex == 17) {
                    boldStyle = true
                } else if (colorIndex == 20) {
                    italicStyle = true
                } else if (colorIndex == 21) {
                    boldStyle = false
                    italicStyle = false
                }
                i++
                on = false
            } else {
                if (on) i--
                character = text[i]
                currentPage = currentGlyphPage
                width = (width + (currentPage.getWidth(character) - 8)).toInt()
            }
            i++
        }
        return width / 2
    }

    /**
     * Trims a string to fit a specified Width.
     */
    fun trimStringToWidth(text: String, width: Int): String {
        return this.trimStringToWidth(text, width, false)
    }

    /**
     * Trims a string to a specified width, and will reverse it if par3 is set.
     */
    private fun trimStringToWidth(text: String, maxWidth: Int, reverse: Boolean): String {
        val stringbuilder = StringBuilder()
        var on = false
        val j = if (reverse) text.length - 1 else 0
        val k = if (reverse) -1 else 1
        var width = 0
        var i = j
        while (i >= 0 && i < text.length && i < maxWidth) {
            var character = text[i]
            if (character == '§') on = true else if (on && character >= '0' && character <= 'r') {
                val colorIndex = "0123456789abcdefklmnor".indexOf(character)
                if (colorIndex < 16) {
                    boldStyle = false
                    italicStyle = false
                } else if (colorIndex == 17) {
                    boldStyle = true
                } else if (colorIndex == 20) {
                    italicStyle = true
                } else if (colorIndex == 21) {
                    boldStyle = false
                    italicStyle = false
                }
                i++
                on = false
            } else {
                if (on) i--
                character = text[i]
                width = (width + (currentGlyphPage.getWidth(character) - 8) / 2).toInt()
            }
            if (i > width) {
                break
            }
            if (reverse) {
                stringbuilder.insert(0, character)
            } else {
                stringbuilder.append(character)
            }
            i += k
        }
        return stringbuilder.toString()
    }

    companion object {
        @Throws(IOException::class, FontFormatException::class)
        fun create(fontLocation: ResourceLocation?, size: Float): GlyphPageFontRenderer {
            val chars = CharArray(256)
            for (i in chars.indices) {
                chars[i] = i.toChar()
            }
            val regularPage: GlyphPage
            val inputStream = mc.resourceManager.getResource(fontLocation).inputStream
            val font = Font.createFont(Font.PLAIN, inputStream)
            regularPage = GlyphPage(font.deriveFont(size), isAntiAliasingEnabled = true, isFractionalMetricsEnabled = true)
            inputStream.close()
            regularPage.generateGlyphPage(chars)
            regularPage.setupTexture()
            return GlyphPageFontRenderer(regularPage, regularPage, regularPage, regularPage)
        }
    }
}