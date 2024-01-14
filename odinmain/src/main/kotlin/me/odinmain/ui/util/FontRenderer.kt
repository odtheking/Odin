package me.odinmain.ui.util

import me.odinmain.OdinMain.mc
import me.odinmain.utils.render.Color
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.MathHelper
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11
import org.lwjgl.util.vector.Vector2f
import java.awt.Font
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.nio.Buffer


class FontRenderer(path: String, size: Float) {
    private val font: Font

    private lateinit var regularData: Array<CharacterData?>
    private lateinit var boldData: Array<CharacterData?>
    private lateinit var italicsData: Array<CharacterData?>
    private var generatedCharData = false

    private val colorCodes = IntArray(32)

    init {
        val inputStream = this::class.java.getResourceAsStream(path)
        this.font = Font.createFont(0, inputStream).deriveFont(Font.PLAIN, size)

        mc.addScheduledTask {
            regularData = setup(arrayOfNulls(256), Font.PLAIN)
            boldData = setup(arrayOfNulls(256), Font.BOLD)
            italicsData = setup(arrayOfNulls(256), Font.ITALIC)

            generatedCharData = true
        }
    }

    private fun setup(characterData: Array<CharacterData?>, type: Int): Array<CharacterData?> {
        generateColors()

        val font = font.deriveFont(type)

        val utilityImage = BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB)
        val utilityGraphics = (utilityImage.graphics as Graphics2D).also { it.font = font }

        val fontMetrics = utilityGraphics.fontMetrics

        for (index in characterData.indices) {
            val character = index.toChar()
            val characterBounds = fontMetrics.getStringBounds(character.toString() + "", utilityGraphics)

            val width = characterBounds.width.toFloat() + 2 * MARGIN
            val height = characterBounds.height.toFloat()

            val characterImage = BufferedImage(
                MathHelper.ceiling_double_int(width.toDouble()),
                MathHelper.ceiling_double_int(height.toDouble()),
                BufferedImage.TYPE_INT_ARGB
            )

            val graphics = (characterImage.graphics as Graphics2D).also {
                it.font = font
                it.color = Color(255, 255, 255, 0f).javaColor
                it.fillRect(0, 0, characterImage.width, characterImage.height)
                it.color = Color.WHITE.javaColor
            }

            graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
            graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON)

            graphics.drawString(character.toString() + "", MARGIN, fontMetrics.ascent)

            val textureId = GlStateManager.generateTexture()
            createTexture(textureId, characterImage)

            characterData[index] = CharacterData(
                characterImage.width.toFloat(),
                characterImage.height.toFloat(),
                textureId
            )
        }

        return characterData
    }

    private fun createTexture(textureId: Int, image: BufferedImage) {
        val pixels = IntArray(image.width * image.height)

        image.getRGB(0, 0, image.width, image.height, pixels, 0, image.width)

        val buffer = BufferUtils.createByteBuffer(image.width * image.height * 4)
        for (y in 0 until image.height) {
            for (x in 0 until image.width) {
                val pixel = pixels[y * image.width + x]

                buffer.put((pixel shr 16 and 0xFF).toByte())
                buffer.put((pixel shr 8 and 0xFF).toByte())
                buffer.put((pixel and 0xFF).toByte())
                buffer.put((pixel shr 24 and 0xFF).toByte())
            }
        }
        (buffer as Buffer).flip()

        GlStateManager.bindTexture(textureId)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST)

        GL11.glTexImage2D(
            GL11.GL_TEXTURE_2D,
            0,
            GL11.GL_RGBA,
            image.width,
            image.height,
            0,
            GL11.GL_RGBA,
            GL11.GL_UNSIGNED_BYTE,
            buffer
        )

        GlStateManager.bindTexture(0)
    }

    /**
     * Renders the given string.
     *
     * @param text  The text to be rendered.
     * @param x     The x position of the text.
     * @param y     The y position of the text.
     * @param color The color of the text.
     */
    fun drawString(text: String, x: Float, y: Float, color: Color) = renderString(text, x, y, color, false)

    /**
     * Renders the given string with a shadow.
     *
     * @param text  The text to be rendered.
     * @param x     The x position of the text.
     * @param y     The y position of the text.
     * @param color The color of the text.
     */
    fun drawStringWithShadow(text: String, x: Float, y: Float, color: Color) {
        GlStateManager.translate(0.5, 0.5, 0.0)
        renderString(text, x, y, color, true)

        GlStateManager.translate(-0.5, -0.5, 0.0)
        renderString(text, x, y, color, false)
    }

    private fun renderString(text: String, xPos: Float, yPos: Float, color: Color, shadow: Boolean) {
        if (text.isEmpty() || !generatedCharData) return
        var x = xPos
        var y = yPos

        GlStateManager.pushMatrix()
        GlStateManager.scale(0.5, 0.5, 1.0)

        x -= (MARGIN / 2).toFloat()
        y -= (MARGIN / 2).toFloat()

        x += 0.5f
        y += 0.5f

        x *= 2f
        y *= 2f

        var characterData = regularData
        var underlined = false
        var strikethrough = false
        var obfuscated = false

        val length = text.length

        val multiplier = (if (shadow) 4 else 1).toFloat()
        val a = color.a / 255f
        val r = color.r / 255f
        val g = color.g / 255f
        val b = color.b/ 255f
        GlStateManager.color(r / multiplier, g / multiplier, b / multiplier, a)

        for (i in 0 until length) {
            var character = text[i]
            val previous = if (i > 0) text[i - 1] else '.'

            if (previous == COLOR_INVOKER) continue
            if (character == COLOR_INVOKER) {
                var index = "0123456789abcdefklmnor".indexOf(text.lowercase()[i + 1])

                when {
                    index < 16 -> {
                        obfuscated = false
                        strikethrough = false
                        underlined = false

                        characterData = regularData

                        if (shadow) index += 16

                        val textColor = colorCodes[index]
                        GlStateManager.color(
                            (textColor shr 16) / 255F,
                            (textColor shr 8 and 255) / 255F,
                            (textColor and 255) / 255F,
                            a
                        )
                    }
                    index == 16 -> obfuscated = true
                    index == 17 -> characterData = boldData
                    index == 18 -> strikethrough = true
                    index == 19 -> underlined = true
                    index == 20 -> characterData = italicsData
                    index == 21 -> {
                        obfuscated = false
                        strikethrough = false
                        underlined = false

                        characterData = regularData

                        GlStateManager.color(
                            if (shadow) 0.25F else 1F,
                            if (shadow) 0.25F else 1F,
                            if (shadow) 0.25F else 1F,
                            a
                        )
                    }
                }
            } else {
                if (character.code > 255) continue
                if (obfuscated) character = (character.code + RANDOM_OFFSET).toChar()

                drawChar(character, characterData, x, y)

                val charData = characterData[character.code]

                if (strikethrough) drawLine(
                    Vector2f(0F, charData!!.height / 2f), Vector2f(
                        charData.width, charData.height / 2f
                    )
                )
                if (underlined) drawLine(
                    Vector2f(0F, charData!!.height - 15), Vector2f(
                        charData.width, charData.height - 15
                    )
                )

                x += charData!!.width - 2 * MARGIN
            }
        }
        GlStateManager.popMatrix()

        GlStateManager.color(1F, 1F, 1F, 1F)
        GlStateManager.bindTexture(0)
    }

    /**
     * Gets the width of the given text.
     *
     * @param text The text to get the width of.
     * @return The width of the given text.
     */
    fun getWidth(text: String): Float {
        if (!generatedCharData) return 0F

        var width = 0f

        var characterData = regularData
        val length = text.length

        for (i in 0 until length) {
            val character = text[i]
            val previous = if (i > 0) text[i - 1] else '.'

            if (previous == COLOR_INVOKER) continue
            if (character == COLOR_INVOKER) {
                when ("0123456789abcdefklmnor".indexOf(text.lowercase()[i + 1])) {
                    17 -> characterData = boldData
                    20 -> characterData = italicsData
                    21 -> characterData = regularData
                    else -> {}
                }
            } else {
                if (character.code > 255) continue

                val charData = characterData[character.code]
                width += (charData!!.width - 2 * MARGIN) / 2
            }
        }

        return width + MARGIN / 2
    }

    /**
     * Gets the height of the given text.
     *
     * @param text The text to get the height of.
     * @return The height of the given text.
     */
    fun getHeight(text: String): Float {
        if (!generatedCharData) return 0F

        var height = 0f

        var characterData = regularData
        val length = text.length

        for (i in 0 until length) {
            val character = text[i]
            val previous = if (i > 0) text[i - 1] else '.'

            if (previous == COLOR_INVOKER) continue
            if (character == COLOR_INVOKER) {
                when ("0123456789abcdefklmnor".indexOf(text.lowercase()[i + 1])) {
                    17 -> characterData = boldData
                    20 -> characterData = italicsData
                    21 -> characterData = regularData
                    else -> {}
                }
            } else {
                if (character.code > 255) continue

                val charData = characterData[character.code]
                height = height.coerceAtLeast(charData!!.height)
            }
        }

        return height / 2 - MARGIN / 2
    }

    private fun drawChar(character: Char, characterData: Array<CharacterData?>, x: Float, y: Float) {
        val charData = characterData[character.code]

        charData!!.bind()
        GlStateManager.pushMatrix()
        GlStateManager.enableBlend()
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)

        GL11.glBegin(GL11.GL_QUADS)
        GL11.glTexCoord2f(0f, 0f)
        GL11.glVertex2d(x.toDouble(), y.toDouble())
        GL11.glTexCoord2f(0f, 1f)
        GL11.glVertex2d(x.toDouble(), (y + charData.height).toDouble())
        GL11.glTexCoord2f(1f, 1f)
        GL11.glVertex2d((x + charData.width).toDouble(), (y + charData.height).toDouble())
        GL11.glTexCoord2f(1f, 0f)
        GL11.glVertex2d((x + charData.width).toDouble(), y.toDouble())
        GL11.glEnd()

        GlStateManager.disableBlend()
        GlStateManager.popMatrix()
        GlStateManager.bindTexture(0)
    }

    private fun drawLine(start: Vector2f, end: Vector2f) {
        GlStateManager.disableTexture2D()
        GL11.glLineWidth(3F)

        GL11.glBegin(GL11.GL_LINES)
        GL11.glVertex2f(start.x, start.y)
        GL11.glVertex2f(end.x, end.y)
        GL11.glEnd()

        GlStateManager.enableTexture2D()
    }

    private fun generateColors() {
        for (i in 0..31) {
            val thing = (i shr 3 and 1) * 85

            var red = (i shr 2 and 1) * 170 + thing
            var green = (i shr 1 and 1) * 170 + thing
            var blue = (i shr 0 and 1) * 170 + thing

            if (i == 6) red += 85
            if (i >= 16) {
                red /= 4
                green /= 4
                blue /= 4
            }

            colorCodes[i] = red and 255 shl 16 or (green and 255 shl 8) or (blue and 255)
        }
    }

    internal inner class CharacterData(var width: Float, var height: Float, private val textureId: Int) {
        fun bind() = GlStateManager.bindTexture(textureId)
    }

    companion object {
        private const val MARGIN = 4
        private const val COLOR_INVOKER = '\u00a7'
        private const val RANDOM_OFFSET = 1
    }
}