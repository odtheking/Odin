package com.odtheking.odin.clickgui.settings.impl

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.clickgui.GuiTheme
import com.odtheking.odin.clickgui.settings.RenderableSetting
import com.odtheking.odin.clickgui.settings.Saving
import com.odtheking.odin.clickgui.widget.isOver
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.render.Corners
import com.odtheking.odin.utils.render.circle
import com.odtheking.odin.utils.render.roundedRect
import com.odtheking.odin.utils.render.roundedRectClipped
import com.odtheking.odin.utils.ui.animations.Fade
import com.odtheking.odin.utils.ui.animations.Tween
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.input.KeyEvent
import net.minecraft.client.input.MouseButtonEvent
import org.lwjgl.glfw.GLFW
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
) : RenderableSetting<E>(name, desc, GuiTheme.ROW_HEIGHT + EXTRA_HEIGHT), Saving where E : Number, E : Comparable<E> {

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

    override var value: E = default
        set(value) {
            field = (round(value.toDouble() / step) * step).coerceIn(minimum, maximum) as E
            display = format(field)
        }

    var display: String = format(default)
        private set

    private val sliderAnim = Tween(SLIDE_DURATION)
    private val knobGrow = Fade(GROW_DURATION)
    private var dragging = false

    private var dragged = false

    init {
        value = default
        sliderAnim.snap(percent)
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

    override fun render(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int) {
        drawLabel(graphics)

        graphics.text(mc.font, display, x + width - mc.font.width(display) - VALUE_PAD, GuiTheme.textY(y, GuiTheme.ROW_HEIGHT), Colors.WHITE.rgba, false)

        val trackX = x + GuiTheme.PADDING
        val trackY = y + TRACK_OFFSET
        val trackWidth = width - GuiTheme.PADDING * 2
        graphics.roundedRect(trackX, trackY, trackX + trackWidth, trackY + TRACK_HEIGHT, GuiTheme.surface.rgba, TRACK_RADIUS)

        if (dragged) sliderAnim.snap(percent) else sliderAnim.target(percent)
        val filled = (sliderAnim.value * trackWidth).roundToInt()
        if (filled > 0) {
            graphics.roundedRectClipped(
                trackX, trackY, trackX + trackWidth, trackY + TRACK_HEIGHT,
                trackX, trackY, trackX + filled, trackY + TRACK_HEIGHT,
                GuiTheme.accent.rgba, Corners(TRACK_RADIUS)
            )
        }

        val overSlider = dragging || isOver(mouseX, mouseY, x, y + height / 2, width, height / 2)
        val radius = knobGrow.lerp(overSlider, KNOB_RADIUS, KNOB_RADIUS + KNOB_GROWTH)
        graphics.circle(trackX + filled, trackY + TRACK_HEIGHT / 2, radius, Colors.WHITE.rgba)
    }

    override fun onClick(event: MouseButtonEvent, doubleClick: Boolean) {
        if (event.y().toInt() < y + height / 2) return
        dragging = true
        dragged = false
        seek(event.x().toInt())
    }

    override fun onDrag(event: MouseButtonEvent, dragX: Double, dragY: Double) {
        if (!dragging) return
        dragged = true
        seek(event.x().toInt())
    }

    override fun onRelease(event: MouseButtonEvent) {
        dragging = false
        dragged = false
    }

    override fun keyPressed(event: KeyEvent): Boolean {
        val steps = when (event.key) {
            GLFW.GLFW_KEY_RIGHT, GLFW.GLFW_KEY_EQUAL -> 1
            GLFW.GLFW_KEY_LEFT, GLFW.GLFW_KEY_MINUS -> -1
            else -> return false
        }
        nudge(steps)
        return true
    }

    override fun release() {
        dragging = false
        dragged = false
    }

    private fun seek(mouseX: Int) {
        val trackX = x + GuiTheme.PADDING
        val trackWidth = width - GuiTheme.PADDING * 2
        percent = (mouseX - trackX).toFloat() / trackWidth
    }

    override fun write(gson: Gson): JsonElement = JsonPrimitive(value)

    override fun read(element: JsonElement, gson: Gson) {
        element.asNumber?.let { value = it as E }
    }

    private companion object {
        const val EXTRA_HEIGHT = 8
        const val VALUE_PAD = 4
        const val TRACK_OFFSET = 18
        const val TRACK_HEIGHT = 6
        const val TRACK_RADIUS = 3f
        const val KNOB_RADIUS = 4f
        const val KNOB_GROWTH = 1.5f
        const val SLIDE_DURATION = 100L
        const val GROW_DURATION = 150L
    }
}