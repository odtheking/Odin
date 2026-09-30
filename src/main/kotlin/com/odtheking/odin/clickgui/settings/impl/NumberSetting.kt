package com.odtheking.odin.clickgui.settings.impl

import androidx.compose.runtime.*
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.mojang.blaze3d.platform.InputConstants
import com.odtheking.odin.clickgui.GuiTheme
import com.odtheking.odin.clickgui.settings.RenderableSetting
import com.odtheking.odin.clickgui.settings.Saving
import com.odtheking.odin.clickgui.ui.SettingRow
import com.odtheking.odin.clickgui.ui.animateProgress
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.render.Corners
import com.odtheking.odin.utils.render.circle
import com.odtheking.odin.utils.render.roundedRect
import com.odtheking.odin.utils.render.roundedRectClipped
import com.odtheking.odin.utils.ui.compose.*
import net.minecraft.client.input.MouseButtonEvent
import kotlin.math.round
import kotlin.math.roundToInt

/**
 * Setting that lets you pick a number between a range.
 * @author Stivais, Aton
 */
@Suppress("UNCHECKED_CAST")
class NumberSetting<E>(
    name: String,
    override val default: E = 1.0 as E,
    range: ClosedFloatingPointRange<Double>,
    increment: Number = 1,
    desc: String,
    private val unit: String = ""
) : RenderableSetting<E>(name, desc), Saving where E : Number, E : Comparable<E> {

    constructor(
        name: String,
        default: E,
        range: IntRange,
        increment: Number = 1,
        desc: String,
        unit: String = ""
    ) : this(name, default, range.first.toDouble()..range.last.toDouble(), increment, desc, unit)

    private val step = increment.toDouble()
    private val minimum = range.start
    private val maximum = range.endInclusive

    private var current by mutableStateOf(default)

    override var value: E
        get() = current
        set(value) {
            current = (round(value.toDouble() / step) * step).coerceIn(minimum, maximum) as E
            display = format(current)
        }

    var display: String = format(default)
        private set

    init {
        value = default
    }

    var percent: Float
        get() = ((value.toDouble() - minimum) / (maximum - minimum)).toFloat()
        set(percent) {
            value = (minimum + percent.coerceIn(0f, 1f) * (maximum - minimum)) as E
        }

    private fun format(value: E): String {
        val current = value.toDouble()
        return if (current % 1.0 == 0.0) "${current.toInt()}$unit"
        else "${(current * 100).roundToInt() / 100.0}$unit"
    }

    fun nudge(steps: Int) {
        value = (value.toDouble() + steps * step).coerceIn(minimum, maximum) as E
    }

    @Composable
    override fun Content() {
        Column {
            SettingRow(height = 18) { Text({ display }) }.clickable {}
            Slider(percent, { percent = it }).size(GuiTheme.INNER_WIDTH, 11).offset({ GuiTheme.PADDING })
        }
            .onKey { event ->
                val steps = when (event.key) {
                    InputConstants.KEY_RIGHT, InputConstants.KEY_EQUALS -> 1
                    InputConstants.KEY_LEFT, InputConstants.KEY_MINUS -> -1
                    else -> return@onKey false
                }
                nudge(steps)
                true
            }
    }

    override fun write(gson: Gson): JsonElement = JsonPrimitive(value)

    override fun read(element: JsonElement, gson: Gson) {
        element.asNumber?.let { value = it as E }
    }
}

@Composable
private fun Slider(value: Float, onValueChange: (Float) -> Unit): UiNode {
    val interaction = remember { InteractionSource() }
    var dragging by remember { mutableStateOf(false) }
    val fill = remember { Animatable(value) }
    SideEffect { if (dragging) fill.snapTo(value) else fill.animateTo(value, 200) }
    val knob = animateProgress { dragging || interaction.hovered }

    val seek: UiNode.(MouseButtonEvent) -> Unit = { event -> onValueChange(((event.x() - x) / width).toFloat().coerceIn(0f, 1f)) }

    return Canvas { graphics ->
        val top = y + (height - TRACK_HEIGHT) / 2
        graphics.roundedRect(x, top, right, top + TRACK_HEIGHT, GuiTheme.surface.rgba, TRACK_CORNERS)

        val filled = (fill.value * width).roundToInt()
        if (filled > 0) graphics.roundedRectClipped(x, top, right, top + TRACK_HEIGHT, x, top, x + filled, top + TRACK_HEIGHT, GuiTheme.accent.rgba, TRACK_CORNERS)
        graphics.circle(x + filled, top + TRACK_HEIGHT / 2, 4f + 1.5f * knob.value, Colors.WHITE.rgba)
    }
        .hoverable(interaction)
        .pointerInput(
            onDrag = {
                dragging = true
                seek(it)
            },
            onRelease = { dragging = false },
        ) { event -> (event.button() == InputConstants.MOUSE_BUTTON_LEFT).also { if (it) seek(event) } }
        .onFocusChanged { if (!it) dragging = false }
}

private const val TRACK_HEIGHT = 6
private val TRACK_CORNERS = Corners(3f)