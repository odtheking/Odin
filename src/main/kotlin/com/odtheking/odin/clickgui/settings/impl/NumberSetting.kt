package com.odtheking.odin.clickgui.settings.impl

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.odtheking.odin.clickgui.ClickGUI.gray38
import com.odtheking.odin.clickgui.Panel
import com.odtheking.odin.clickgui.settings.RenderableSetting
import com.odtheking.odin.clickgui.settings.Saving
import com.odtheking.odin.features.impl.render.ClickGUIModule
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.ui.HoverHandler
import com.odtheking.odin.utils.ui.animations.LinearAnimation
import com.odtheking.odin.utils.ui.isAreaHovered
import com.odtheking.odin.utils.ui.rendering.NVGRenderer
import net.minecraft.client.input.KeyEvent
import net.minecraft.client.input.MouseButtonEvent
import org.lwjgl.glfw.GLFW
import kotlin.math.floor
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
    min: Number,
    max: Number,
    increment: Number = 1,
    desc: String,
    private val unit: String = ""
) : RenderableSetting<E>(name, desc), Saving where E : Number, E : Comparable<E> {

    private val incrementDouble = increment.toDouble()
    private val minDouble = min.toDouble()
    private var maxDouble = max.toDouble()

    private val sliderAnim = LinearAnimation<Float>(100)
    private val handler = HoverHandler(150)

    private var displayValue = ""
    private var prevLocation = 0f
    private var valueWidth = -1f
    private var isDragging = false

    private var sliderPercentage = 0f
        set(value) {
            if (sliderPercentage != value) {
                if (!isDragging) {
                    prevLocation = sliderAnim.get(prevLocation, sliderPercentage, false)
                    sliderAnim.start()
                }
                displayValue = getDisplay()
                valueWidth = -1f
            }
            field = value
        }

    override var value: E = default
        set(value) {
            field = roundToIncrement(value).coerceIn(minDouble, maxDouble) as E
            sliderPercentage = ((field.toDouble() - minDouble) / (maxDouble - minDouble)).toFloat()
        }

    init {
        value = default
        displayValue = getDisplay()
    }

    private var valueDouble
        get() = value.toDouble()
        set(value) {
            this.value = value as E
        }

    private var valueInt
        get() = value.toInt()
        set(value) {
            this.value = value as E
        }

    override fun render(x: Float, y: Float, mouseX: Float, mouseY: Float): Float {
        super.render(x, y, mouseX, mouseY)
        val height = getHeight()

        handler.handle(x, y + height / 2, width, height / 2, true)

        if (listening) {
            val newPercentage = ((mouseX - (x + 6f)) / (width - 12f)).coerceIn(0f, 1f)
            valueDouble = minDouble + newPercentage * (maxDouble - minDouble)
            sliderPercentage = newPercentage
        }

        if (valueWidth < 0) {
            valueWidth = NVGRenderer.textWidth(displayValue, 16f, NVGRenderer.defaultFont)
        }

        NVGRenderer.text(name, x + 6f, y + height / 2f - 15f, 16f, Colors.WHITE.rgba, NVGRenderer.defaultFont)
        NVGRenderer.text(displayValue, x + width - valueWidth - 4f, y + height / 2f - 15f, 16f, Colors.WHITE.rgba, NVGRenderer.defaultFont)

        NVGRenderer.rect(x + 6f, y + 24f, width - 12f, 8f, gray38.rgba, 3f)

        if (x + sliderPercentage * (width - 12f) > x + 6)
            NVGRenderer.rect(x + 6f, y + 24f, sliderAnim.get(prevLocation, sliderPercentage, false) * (width - 12f), 8f, ClickGUIModule.clickGUIColor.rgba, 3f)

        NVGRenderer.circle(x + 6f + sliderAnim.get(prevLocation, sliderPercentage, false) * (width - 12f), y + 28f, handler.anim.get(7f, 9f, !isHovered), Colors.WHITE.rgba)

        return height
    }

    override fun mouseClicked(mouseX: Float, mouseY: Float, click: MouseButtonEvent): Boolean {
        return if (click.button() != 0 || !isHovered) false
        else {
            listening = true
            isDragging = true
            prevLocation = sliderPercentage
            sliderAnim.start()
            true
        }
    }

    override fun mouseReleased(click: MouseButtonEvent) {
        listening = false
        if (isDragging) {
            isDragging = false
            prevLocation = sliderAnim.get(prevLocation, sliderPercentage, false)
            sliderAnim.start()
        }
    }

    override fun keyPressed(input: KeyEvent): Boolean {
        if (!isHovered) return false

        val amount = when (input.key) {
            GLFW.GLFW_KEY_RIGHT, GLFW.GLFW_KEY_EQUAL -> incrementDouble
            GLFW.GLFW_KEY_LEFT, GLFW.GLFW_KEY_MINUS -> -incrementDouble
            else -> return false
        }

        if (valueDouble !in minDouble..maxDouble) return false
        valueDouble = (valueDouble + amount).coerceIn(minDouble, maxDouble)
        sliderPercentage = ((valueDouble - minDouble) / (maxDouble - minDouble)).toFloat()
        return true
    }

    override val isHovered: Boolean
        get() =
            isAreaHovered(lastX, lastY + getHeight() / 2, width, getHeight() / 2, true)

    override fun getHeight(): Float = Panel.HEIGHT + 8f

    override fun write(gson: Gson): JsonElement = JsonPrimitive(value)

    override fun read(element: JsonElement, gson: Gson) {
        element.asNumber?.let { value = it as E }
    }

    private fun roundToIncrement(x: Number): Double =
        round((x.toDouble() / incrementDouble)) * incrementDouble

    private fun getDisplay(): String =
        if (valueDouble - floor(valueDouble) == 0.0)
            "${(valueInt * 100.0).roundToInt() / 100}${unit}"
        else
            "${(valueDouble * 100.0).roundToInt() / 100.0}${unit}"
}