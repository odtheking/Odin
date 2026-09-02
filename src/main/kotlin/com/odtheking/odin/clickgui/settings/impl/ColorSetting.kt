package com.odtheking.odin.clickgui.settings.impl

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.clickgui.GuiTheme
import com.odtheking.odin.clickgui.settings.RenderableSetting
import com.odtheking.odin.clickgui.settings.Saving
import com.odtheking.odin.clickgui.widget.isOver
import com.odtheking.odin.clickgui.widget.stripChrome
import com.odtheking.odin.features.impl.render.ClickGUIModule
import com.odtheking.odin.utils.Color
import com.odtheking.odin.utils.Color.Companion.darker
import com.odtheking.odin.utils.Color.Companion.hsbMax
import com.odtheking.odin.utils.Color.Companion.withAlpha
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.render.*
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

    override val clickButtons: IntArray = BOTH_BUTTONS

    private val expand = Fade(EXPAND_DURATION, Easing.EASE_IN_OUT)
    private val saturationMarker = Tween(MARKER_DURATION)
    private val brightnessMarker = Tween(MARKER_DURATION)
    private val hueMarker = Tween(MARKER_DURATION)
    private val alphaMarker = Tween(MARKER_DURATION)

    private var holding: Slider? = null

    private val hexInput by lazy {
        HexEditBox(mc.font, HEX_BOX_WIDTH - HEX_INSET * 2, HEX_FIELD_HEIGHT, Component.literal(name))
            .stripChrome(true)
            .apply {
                setMaxLength(hexLength)
                value = hex
                setResponder(::applyHex)
            }
    }

    private val barX get() = x + GuiTheme.PADDING
    private val barWidth get() = width - GuiTheme.PADDING * 2

    private val squareWidth: Int
        get() {
            var w = barWidth - GAP - BAR_THICKNESS
            if (allowAlpha) w -= GAP + BAR_THICKNESS
            return w
        }

    private val hueBarX get() = barX + squareWidth + GAP
    private val alphaBarX get() = hueBarX + BAR_THICKNESS + GAP

    private val favoritesLeft get() = barX + barWidth - FAVORITE_SLOTS * FAVORITE_SIZE - (FAVORITE_SLOTS - 1) * GAP

    private fun favoriteSlotX(index: Int) = favoritesLeft + index * (FAVORITE_SIZE + GAP)

    private val hexBoxLeft get() = barX
    private val hexBoxRight get() = hexBoxLeft + HEX_BOX_WIDTH

    override fun children(): List<GuiEventListener> = if (expand.current) listOf(hexInput) else emptyList()

    override fun measure() {
        height = expand.lerp(GuiTheme.ROW_HEIGHT, GuiTheme.ROW_HEIGHT + EXPANDED)
    }

    private var pushedHex = hex

    override fun place() {
        hexInput.x = hexBoxLeft + HEX_INSET
        hexInput.y = GuiTheme.textY(hexBoxY, HEX_BOX_HEIGHT)

        val current = hex
        if (current == pushedHex) return
        pushedHex = current
        if (!hexInput.isFocused) hexInput.value = current
    }

    private val hexBoxY get() = y + GuiTheme.ROW_HEIGHT + EXPANDED - HEX_BOTTOM_PAD

    override fun render(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int) {
        drawLabel(graphics)
        val left = swatchX
        val top = swatchY
        graphics.roundedRectOutlined(left, top, left + SWATCH_WIDTH, top + SWATCH_HEIGHT, value.rgba, value.withAlpha(1f).darker().rgba, 1.5f, GuiTheme.RADIUS)

        renderExpanded(graphics) {
            drawSaturationSquare(graphics)
            drawHueBar(graphics)
            if (allowAlpha) drawAlphaBar(graphics)
            drawHexBox(graphics)
            drawFavorites(graphics)
            renderChildren(graphics, mouseX, mouseY)
        }
    }

    override fun onClick(event: MouseButtonEvent, doubleClick: Boolean) {
        val mouseX = event.x().toInt()
        val mouseY = event.y().toInt()

        if (isOver(mouseX, mouseY, swatchX, swatchY, SWATCH_WIDTH, SWATCH_HEIGHT)) {
            expand.toggle()
            if (!expand.current) setFocused(null)
            return
        }
        if (!expand.current) return

        if (isOver(mouseX, mouseY, hexBoxLeft, hexBoxY, HEX_BOX_WIDTH, HEX_BOX_HEIGHT)) {
            setFocused(hexInput)
            hexInput.onClick(event, doubleClick)
            return
        }

        if (onFavoriteClick(event)) {
            setFocused(null)
            return
        }
        if (event.button() != LEFT) return

        holding = when {
            isOver(mouseX, mouseY, barX, y + SQUARE_TOP, squareWidth, SQUARE_HEIGHT) -> Slider.SATURATION
            isOver(mouseX, mouseY, hueBarX, y + SQUARE_TOP, BAR_THICKNESS, SQUARE_HEIGHT) -> Slider.HUE
            allowAlpha && isOver(mouseX, mouseY, alphaBarX, y + SQUARE_TOP, BAR_THICKNESS, SQUARE_HEIGHT) -> Slider.ALPHA
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
        expand.progress(false)
        setFocused(null)
    }

    private val swatchX get() = x + width - SWATCH_RIGHT
    private val swatchY get() = y + (GuiTheme.ROW_HEIGHT - SWATCH_HEIGHT) / 2

    private fun drawSaturationSquare(graphics: GuiGraphicsExtractor) {
        val left = barX
        val right = left + squareWidth
        val top = y + SQUARE_TOP
        val bottom = top + SQUARE_HEIGHT
        val corners = BAR_CORNERS

        graphics.roundedGradient(left, top, right, bottom, Colors.WHITE.rgba, value.hsbMax().rgba, GradientDirection.LEFT_TO_RIGHT, corners)
        graphics.roundedGradient(left, top, right, bottom, Colors.TRANSPARENT.rgba, Colors.BLACK.rgba, GradientDirection.TOP_TO_BOTTOM, corners)

        saturationMarker.target(value.saturation)
        brightnessMarker.target(value.brightness)
        val markerX = (left + saturationMarker.value * squareWidth).roundToInt()
        val markerY = (top + (1f - brightnessMarker.value) * SQUARE_HEIGHT).roundToInt()
        graphics.circle(markerX, markerY, MARKER_RADIUS, Colors.WHITE.rgba)
    }

    private fun drawHueBar(graphics: GuiGraphicsExtractor) {
        val left = hueBarX
        val top = y + SQUARE_TOP
        graphics.roundedTexture(left, top, left + BAR_THICKNESS, top + SQUARE_HEIGHT, HUE_GRADIENT, corners = BAR_CORNERS)
        hueMarker.target(value.hue)
        drawBarMarker(graphics, left, top, hueMarker.value)
    }

    private fun drawAlphaBar(graphics: GuiGraphicsExtractor) {
        val left = alphaBarX
        val top = y + SQUARE_TOP
        graphics.roundedGradient(
            left, top, left + BAR_THICKNESS, top + SQUARE_HEIGHT,
            Colors.TRANSPARENT.rgba, value.withAlpha(1f).rgba, GradientDirection.TOP_TO_BOTTOM,
            BAR_CORNERS
        )

        alphaMarker.target(value.alphaFloat)
        drawBarMarker(graphics, left, top, alphaMarker.value)
    }

    private fun drawBarMarker(graphics: GuiGraphicsExtractor, left: Int, top: Int, progress: Float) {
        val markerY = (top + progress * SQUARE_HEIGHT).roundToInt()
        graphics.roundedRect(left - 1, markerY - 2, left + BAR_THICKNESS + 1, markerY + 2, Colors.WHITE.rgba, 2f)
    }

    private fun drawHexBox(graphics: GuiGraphicsExtractor) {
        val left = hexBoxLeft
        val top = hexBoxY
        val right = hexBoxRight
        val bottom = top + HEX_BOX_HEIGHT
        graphics.roundedRectOutlined(left, top, right, bottom, GuiTheme.surface.rgba, GuiTheme.accent.rgba, 1f, GuiTheme.RADIUS)
    }

    private fun drawFavorites(graphics: GuiGraphicsExtractor) {
        val top = hexBoxY
        val bottom = top + FAVORITE_SIZE

        for (i in 0 until FAVORITE_SLOTS) {
            val left = favoriteSlotX(i)
            val right = left + FAVORITE_SIZE
            val fill = ClickGUIModule.favoriteColors[i]?.rgba ?: GuiTheme.background.withAlpha(0.4f).rgba

            graphics.roundedRectOutlined(left, top, right, bottom, fill, GuiTheme.surface.rgba, 1f, FAVORITE_RADIUS)
        }
    }

    private fun onFavoriteClick(event: MouseButtonEvent): Boolean {
        val mouseX = event.x().toInt()
        val mouseY = event.y().toInt()
        val top = hexBoxY

        for (i in 0 until FAVORITE_SLOTS) {
            val left = favoriteSlotX(i)
            if (!isOver(mouseX, mouseY, left, top, FAVORITE_SIZE, FAVORITE_SIZE)) continue

            if (event.button() == RIGHT) ClickGUIModule.favoriteColors[i] = value.copy()
            else ClickGUIModule.favoriteColors[i]?.let { value = it.copy() }
            return true
        }
        return false
    }

    private fun seek(mouseX: Int, mouseY: Int) {
        val vertical = ((mouseY - (y + SQUARE_TOP)).toFloat() / SQUARE_HEIGHT).coerceIn(0f, 1f)
        when (holding) {
            Slider.SATURATION -> {
                value.saturation = ((mouseX - barX).toFloat() / squareWidth).coerceIn(0f, 1f)
                value.brightness = 1f - vertical
            }
            Slider.HUE -> value.hue = vertical
            Slider.ALPHA -> value.alphaFloat = vertical
            null -> Unit
        }
    }

    private companion object {
        val HUE_GRADIENT: Identifier = Identifier.fromNamespaceAndPath("odin", "textures/huegradient.png")

        const val EXPAND_DURATION = 200L
        const val MARKER_DURATION = 100L

        const val SQUARE_TOP = GuiTheme.ROW_HEIGHT + 3
        const val SQUARE_HEIGHT = 112
        const val GAP = 3
        const val BAR_THICKNESS = 10
        const val BAR_RADIUS = 2f
        const val MARKER_RADIUS = 4f
        val BAR_CORNERS = Corners(BAR_RADIUS)

        const val EXPANDED = 136

        const val HEX_BOX_HEIGHT = 16
        const val HEX_BOX_WIDTH = 60
        const val HEX_BOTTOM_PAD = 18
        const val HEX_INSET = 5
        const val HEX_FIELD_HEIGHT = 8

        const val FAVORITE_SLOTS = 4
        const val FAVORITE_SIZE = HEX_BOX_HEIGHT
        const val FAVORITE_RADIUS = 3f

        const val SWATCH_WIDTH = 22
        const val SWATCH_HEIGHT = 14
        const val SWATCH_RIGHT = 28
    }
}

private class HexEditBox(font: Font, width: Int, height: Int, message: Component) : EditBox(font, 0, 0, width, height, message) {
    override fun insertText(text: String) {
        super.insertText(text.filter { it.isHex() }.uppercase())
    }
}

fun Char.isHex() = isDigit() || this in 'a'..'f' || this in 'A'..'F'