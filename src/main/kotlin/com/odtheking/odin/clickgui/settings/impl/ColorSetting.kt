package com.odtheking.odin.clickgui.settings.impl

import androidx.compose.runtime.*
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.mojang.blaze3d.platform.InputConstants
import com.odtheking.odin.clickgui.GuiTheme
import com.odtheking.odin.clickgui.settings.RenderableSetting
import com.odtheking.odin.clickgui.settings.Saving
import com.odtheking.odin.clickgui.ui.*
import com.odtheking.odin.features.impl.render.ClickGUIModule
import com.odtheking.odin.utils.Color
import com.odtheking.odin.utils.Color.Companion.darker
import com.odtheking.odin.utils.Color.Companion.hsbMax
import com.odtheking.odin.utils.Color.Companion.withAlpha
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.render.*
import com.odtheking.odin.utils.ui.compose.*
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.resources.Identifier
import kotlin.math.roundToInt

class ColorSetting(
    name: String,
    override val default: Color,
    private val allowAlpha: Boolean = false,
    desc: String
) : RenderableSetting<Color>(name, desc), Saving {

    override var value: Color by mutableStateOf(default.copy())

    private val hexLength = if (allowAlpha) 8 else 6

    private val hex: String get() = value.hex(allowAlpha)

    fun applyHex(hex: String) {
        val digits = hex.filter { it.isHex() }
        if (digits.length != hexLength || digits == this.hex) return
        value = Color(digits.padEnd(8, 'F'))
    }

    @Composable
    override fun Content() {
        var expanded by remember { mutableStateOf(false) }
        val color = value

        Column {
            SettingRow {
                val outline = remember(color) { color.withAlpha(1f).darker().rgba }
                Canvas { graphics ->
                    graphics.roundedRectOutlined(x, y, right, bottom, color.rgba, outline, 1.5f, GuiTheme.RADIUS)
                }.clickable(onClick = { expanded = !expanded }).size(SWATCH_WIDTH, SWATCH_HEIGHT)
            }
            AnimatedVisibility(expanded) {
                Column {
                    Gap(GAP)
                    Row(spacing = GAP) {
                        SaturationBrightness(color).size(PICKER_WIDTH - (BAR_WIDTH + GAP) * (if (allowAlpha) 2 else 1), PICKER_HEIGHT)
                        val hue = animateFloatAsState(color.hue, MARKER_DURATION)
                        Canvas { graphics ->
                            graphics.roundedTexture(x, y, right, bottom, HUE_GRADIENT, corners = BAR_CORNERS)
                            graphics.drawMarker(this, hue.value)
                        }.size(BAR_WIDTH, PICKER_HEIGHT).draggable { _, y -> value = Color(y, value.saturation, value.brightness, value.alphaFloat) }
                        if (allowAlpha) AlphaBar(color)
                    }.offset({ GuiTheme.PADDING })
                    Gap(GAP)
                    Box {
                        TextBox(
                            hex, ::applyHex,
                            maxLength = hexLength, centered = true, filter = { it.filter(Char::isHex).uppercase() },
                        ).size(HEX_WIDTH, FIELD_HEIGHT)
                        Row(spacing = GAP) {
                            repeat(FAVORITE_SLOTS) { slot ->
                                Canvas { graphics ->
                                    val fill = ClickGUIModule.favoriteColors[slot]?.rgba ?: GuiTheme.background.withAlpha(0.4f).rgba
                                    graphics.roundedRectOutlined(x, y, right, bottom, fill, GuiTheme.surface.rgba, 1f, FAVORITE_RADIUS)
                                }.size(FIELD_HEIGHT).clickable(onRightClick = { ClickGUIModule.favoriteColors[slot] = value.copy() }) {
                                    ClickGUIModule.favoriteColors[slot]?.let { value = it.copy() }
                                }
                            }
                        }.offset({ PICKER_WIDTH - FAVORITES_WIDTH })
                    }.offset({ GuiTheme.PADDING })
                    Gap(2)
                }.width(GuiTheme.ROW_WIDTH)
            }
        }
    }

    @Composable
    private fun SaturationBrightness(color: Color): UiNode {
        val saturation = animateFloatAsState(color.saturation, MARKER_DURATION)
        val brightness = animateFloatAsState(color.brightness, MARKER_DURATION)
        val pure = remember(color) { color.hsbMax().rgba }
        return Canvas { graphics ->
            graphics.roundedGradient(x, y, right, bottom, Colors.WHITE.rgba, pure, GradientDirection.LEFT_TO_RIGHT, BAR_CORNERS)
            graphics.roundedGradient(x, y, right, bottom, Colors.TRANSPARENT.rgba, Colors.BLACK.rgba, GradientDirection.TOP_TO_BOTTOM, BAR_CORNERS)
            graphics.circle((x + saturation.value * width).roundToInt(), (y + (1f - brightness.value) * height).roundToInt(), MARKER_RADIUS, Colors.WHITE.rgba)
        }.draggable { x, y -> value = Color(value.hue, x, 1f - y, value.alphaFloat) }
    }

    @Composable
    private fun AlphaBar(color: Color) {
        val alpha = animateFloatAsState(color.alphaFloat, MARKER_DURATION)
        val opaque = remember(color) { color.withAlpha(1f).rgba }
        Canvas { graphics ->
            graphics.roundedGradient(x, y, right, bottom, Colors.TRANSPARENT.rgba, opaque, GradientDirection.TOP_TO_BOTTOM, BAR_CORNERS)
            graphics.drawMarker(this, alpha.value)
        }.size(BAR_WIDTH, PICKER_HEIGHT).draggable { _, y -> value = value.withAlpha(y) }
    }

    override fun write(gson: Gson): JsonElement = gson.toJsonTree(value, Color::class.java)

    override fun read(element: JsonElement, gson: Gson) {
        value = gson.fromJson(element, Color::class.java) ?: default.copy()
    }

    private companion object {
        val HUE_GRADIENT: Identifier = Identifier.fromNamespaceAndPath("odin", "textures/huegradient.png")

        const val MARKER_DURATION = 100
        const val GAP = 3
        const val PICKER_WIDTH = GuiTheme.INNER_WIDTH
        const val PICKER_HEIGHT = 112
        const val BAR_WIDTH = 10
        const val MARKER_RADIUS = 4f
        val BAR_CORNERS = Corners(2f)

        const val FIELD_HEIGHT = 16
        const val HEX_WIDTH = 60
        const val FAVORITE_SLOTS = 4
        const val FAVORITES_WIDTH = FAVORITE_SLOTS * FIELD_HEIGHT + (FAVORITE_SLOTS - 1) * GAP
        const val FAVORITE_RADIUS = 3f

        const val SWATCH_WIDTH = 22
        const val SWATCH_HEIGHT = 14
    }
}

private fun UiNode.draggable(onMove: (x: Float, y: Float) -> Unit): UiNode {
    val move: UiNode.(MouseButtonEvent) -> Unit = { event ->
        onMove(((event.x() - x) / width).toFloat().coerceIn(0f, 1f), ((event.y() - y) / height).toFloat().coerceIn(0f, 1f))
    }
    return pointerInput(onDrag = move) { event -> (event.button() == InputConstants.MOUSE_BUTTON_LEFT).also { if (it) move(event) } }
}

private fun GuiGraphicsExtractor.drawMarker(bar: UiNode, progress: Float) {
    val markerY = (bar.y + progress * bar.height).roundToInt()
    roundedRect(bar.x - 1, markerY - 2, bar.right + 1, markerY + 2, Colors.WHITE.rgba, 2f)
}

fun Char.isHex() = isDigit() || this in 'a'..'f' || this in 'A'..'F'