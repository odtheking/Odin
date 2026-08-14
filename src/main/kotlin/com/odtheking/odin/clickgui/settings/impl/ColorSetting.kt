package com.odtheking.odin.clickgui.settings.impl

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.clickgui.GuiTheme
import com.odtheking.odin.clickgui.settings.RenderableSetting
import com.odtheking.odin.clickgui.settings.Saving
import com.odtheking.odin.clickgui.widget.isOver
import com.odtheking.odin.clickgui.widget.stripChrome
import com.odtheking.odin.utils.Color
import com.odtheking.odin.utils.Color.Companion.darker
import com.odtheking.odin.utils.Color.Companion.hsbMax
import com.odtheking.odin.utils.Color.Companion.withAlpha
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.render.Corners
import com.odtheking.odin.utils.render.GradientDirection
import com.odtheking.odin.utils.render.circle
import com.odtheking.odin.utils.render.pushScissor
import com.odtheking.odin.utils.render.roundedGradient
import com.odtheking.odin.utils.render.roundedRect
import com.odtheking.odin.utils.render.roundedRectOutlined
import com.odtheking.odin.utils.render.roundedTexture
import com.odtheking.odin.utils.ui.animations.Easing
import com.odtheking.odin.utils.ui.animations.Fade
import com.odtheking.odin.utils.ui.animations.Tween
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.components.EditBox
import net.minecraft.client.gui.components.events.GuiEventListener
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import kotlin.math.roundToInt

class ColorSetting(
    name: String,
    override val default: Color,
    private val allowAlpha: Boolean = false,
    desc: String
) : RenderableSetting<Color>(name, desc), Saving {

    override var value: Color = default.copy()

    private val hexLength = if (allowAlpha) 8 else 6

    private val hex: String get() = value.hex(allowAlpha)

    fun applyHex(hex: String) {
        val digits = hex.filter { it.isHex() }
        if (digits.length != hexLength) return
        value = Color(digits.padEnd(8, 'F'))
    }

    override fun write(gson: Gson): JsonElement = gson.toJsonTree(value, Color::class.java)

    override fun read(element: JsonElement, gson: Gson) {
        value = gson.fromJson(element, Color::class.java) ?: default.copy()
    }

    private enum class Slider { SATURATION, HUE, ALPHA }

    private val expand = Fade(EXPAND_DURATION, Easing.EASE_IN_OUT)
    private val saturationMarker = Tween(MARKER_DURATION)
    private val brightnessMarker = Tween(MARKER_DURATION)
    private val hueMarker = Tween(MARKER_DURATION)
    private val alphaMarker = Tween(MARKER_DURATION)

    private var extended = false
    private var holding: Slider? = null

    private val hexInput by lazy {
        HexEditBox(mc.font, GuiTheme.ROW_WIDTH / 2 - HEX_INSET * 2, HEX_FIELD_HEIGHT, Component.literal(name))
            .stripChrome(true)
            .apply {
                setMaxLength(hexLength)
                value = hex
                setResponder(::applyHex)
            }
    }

    private val barX get() = x + GuiTheme.PADDING
    private val barWidth get() = width - GuiTheme.PADDING * 2
    private val expandedHeight get() = if (allowAlpha) EXPANDED_WITH_ALPHA else EXPANDED

    override fun children(): List<GuiEventListener> = if (extended) listOf(hexInput) else emptyList()

    override fun measure() {
        height = expand.lerp(extended, GuiTheme.ROW_HEIGHT, GuiTheme.ROW_HEIGHT + expandedHeight)
    }

    private var pushedHex = hex

    override fun place() {
        hexInput.x = x + width / 4 + HEX_INSET
        hexInput.y = GuiTheme.textY(hexBoxY, HEX_BOX_HEIGHT)

        val current = hex
        if (current == pushedHex) return
        pushedHex = current
        if (!hexInput.isFocused) hexInput.value = current
    }

    private val hexBoxY get() = y + GuiTheme.ROW_HEIGHT + expandedHeight - HEX_BOTTOM_PAD

    override fun render(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int) {
        drawLabel(graphics)
        val left = swatchX
        val top = swatchY
        val right = left + SWATCH_WIDTH
        val bottom = top + SWATCH_HEIGHT
        graphics.roundedRectOutlined(left, top, right, bottom, value.rgba, value.withAlpha(1f).darker().rgba, 1.5f, GuiTheme.RADIUS)

        val revealed = height - GuiTheme.ROW_HEIGHT
        if (revealed <= 0) return

        graphics.pushScissor(x, y + GuiTheme.ROW_HEIGHT, x + width, y + GuiTheme.ROW_HEIGHT + revealed)

        drawSaturationSquare(graphics)
        drawHueBar(graphics)
        if (allowAlpha) drawAlphaBar(graphics)
        drawHexBox(graphics)
        renderChildren(graphics, mouseX, mouseY)

        graphics.disableScissor()
    }

    override fun onClick(event: MouseButtonEvent, doubleClick: Boolean) {
        val mouseX = event.x().toInt()
        val mouseY = event.y().toInt()

        if (isOver(mouseX, mouseY, swatchX, swatchY, SWATCH_WIDTH, SWATCH_HEIGHT)) {
            extended = !extended
            if (!extended) setFocused(null)
            return
        }
        if (!extended) return

        if (isOver(mouseX, mouseY, x + width / 4, hexBoxY, width / 2, HEX_BOX_HEIGHT)) {
            setFocused(hexInput)
            hexInput.onClick(event, doubleClick)
            return
        }

        holding = when {
            isOver(mouseX, mouseY, barX, y + SQUARE_TOP, barWidth, SQUARE_HEIGHT) -> Slider.SATURATION
            isOver(mouseX, mouseY, barX, y + HUE_TOP, barWidth, BAR_HEIGHT) -> Slider.HUE
            allowAlpha && isOver(mouseX, mouseY, barX, y + ALPHA_TOP, barWidth, BAR_HEIGHT) -> Slider.ALPHA
            else -> null
        }
        if (holding != null) {
            setFocused(null)
            seek(mouseX, mouseY)
        }
    }

    override fun onDrag(event: MouseButtonEvent, dragX: Double, dragY: Double) {
        if (holding != null) seek(event.x().toInt(), event.y().toInt())
    }

    override fun onRelease(event: MouseButtonEvent) {
        holding = null
    }

    override fun release() {
        holding = null
        extended = false
        setFocused(null)
    }

    private val swatchX get() = x + width - SWATCH_RIGHT
    private val swatchY get() = y + (GuiTheme.ROW_HEIGHT - SWATCH_HEIGHT) / 2

    private fun drawSaturationSquare(graphics: GuiGraphicsExtractor) {
        val left = barX
        val right = left + barWidth
        val top = y + SQUARE_TOP
        val bottom = top + SQUARE_HEIGHT
        val corners = BAR_CORNERS

        graphics.roundedGradient(left, top, right, bottom, Colors.WHITE.rgba, value.hsbMax().rgba, GradientDirection.LEFT_TO_RIGHT, corners)
        graphics.roundedGradient(left, top, right, bottom, Colors.TRANSPARENT.rgba, Colors.BLACK.rgba, GradientDirection.TOP_TO_BOTTOM, corners)

        saturationMarker.target(value.saturation)
        brightnessMarker.target(value.brightness)
        val markerX = (left + saturationMarker.value * barWidth).roundToInt()
        val markerY = (top + (1f - brightnessMarker.value) * SQUARE_HEIGHT).roundToInt()
        graphics.circle(markerX, markerY, MARKER_RADIUS, Colors.WHITE.rgba)
    }

    private fun drawHueBar(graphics: GuiGraphicsExtractor) {
        val top = y + HUE_TOP
        graphics.roundedTexture(barX, top, barX + barWidth, top + BAR_HEIGHT, HUE_GRADIENT, corners = BAR_CORNERS)
        hueMarker.target(value.hue)
        drawBarMarker(graphics, top, hueMarker.value)
    }

    private fun drawAlphaBar(graphics: GuiGraphicsExtractor) {
        val top = y + ALPHA_TOP
        graphics.roundedGradient(
            barX, top, barX + barWidth, top + BAR_HEIGHT,
            Colors.TRANSPARENT.rgba, value.withAlpha(1f).rgba, GradientDirection.LEFT_TO_RIGHT,
            BAR_CORNERS
        )

        alphaMarker.target(value.alphaFloat)
        drawBarMarker(graphics, top, alphaMarker.value)
    }

    private fun drawBarMarker(graphics: GuiGraphicsExtractor, top: Int, progress: Float) {
        val markerX = (barX + progress * barWidth).roundToInt()
        graphics.roundedRect(markerX - 2, top - 1, markerX + 2, top + BAR_HEIGHT + 1, Colors.WHITE.rgba, 2f)
    }

    private fun drawHexBox(graphics: GuiGraphicsExtractor) {
        val left = x + width / 4
        val top = hexBoxY
        val right = left + width / 2
        val bottom = top + HEX_BOX_HEIGHT
        graphics.roundedRectOutlined(left, top, right, bottom, GuiTheme.surface.rgba, GuiTheme.accent.rgba, 1f, GuiTheme.RADIUS)
    }

    private fun seek(mouseX: Int, mouseY: Int) {
        val horizontal = ((mouseX - barX).toFloat() / barWidth).coerceIn(0f, 1f)
        when (holding) {
            Slider.SATURATION -> {
                value.saturation = horizontal
                value.brightness = (1f - (mouseY - (y + SQUARE_TOP)).toFloat() / SQUARE_HEIGHT).coerceIn(0f, 1f)
            }
            Slider.HUE -> value.hue = horizontal
            Slider.ALPHA -> value.alphaFloat = horizontal
            null -> Unit
        }
    }

    private companion object {
        val HUE_GRADIENT: Identifier = Identifier.fromNamespaceAndPath("odin", "textures/huegradient.png")

        const val EXPAND_DURATION = 200L
        const val MARKER_DURATION = 100L

        const val SQUARE_TOP = GuiTheme.ROW_HEIGHT + 3
        const val SQUARE_HEIGHT = 112
        const val HUE_TOP = SQUARE_TOP + SQUARE_HEIGHT + 3
        const val BAR_HEIGHT = 10
        const val ALPHA_TOP = HUE_TOP + BAR_HEIGHT + 3
        const val BAR_RADIUS = 2f
        const val MARKER_RADIUS = 4f
        val BAR_CORNERS = Corners(BAR_RADIUS)

        const val EXPANDED = 152
        const val EXPANDED_WITH_ALPHA = 168

        const val HEX_BOX_HEIGHT = 16
        const val HEX_BOTTOM_PAD = 18
        const val HEX_INSET = 3
        const val HEX_FIELD_HEIGHT = 8

        const val SWATCH_WIDTH = 22
        const val SWATCH_HEIGHT = 14
        const val SWATCH_RIGHT = 28
    }
}

private class HexEditBox(
    font: Font, width: Int, height: Int, message: Component
) : EditBox(font, 0, 0, width, height, message) {

    override fun insertText(text: String) {
        super.insertText(text.filter { it.isHex() })
    }
}

fun Char.isHex() = isDigit() || this in 'a'..'f' || this in 'A'..'F'