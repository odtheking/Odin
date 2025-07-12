package me.odinmain.clickgui.settings.impl

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import me.odinmain.clickgui.ClickGUI.gray38
import me.odinmain.clickgui.Panel
import me.odinmain.clickgui.settings.RenderableSetting
import me.odinmain.clickgui.settings.Saving
import me.odinmain.features.impl.render.ClickGUIModule
import me.odinmain.utils.render.Colors
import me.odinmain.utils.ui.HoverHandler
import me.odinmain.utils.ui.animations.LinearAnimation
import me.odinmain.utils.ui.isAreaHovered
import me.odinmain.utils.ui.rendering.NVGRenderer
import org.lwjgl.input.Keyboard
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
    min: Number = -10000,
    max: Number = 10000,
    increment: Number = 1,
    desc: String,
    private val unit: String = ""
) : RenderableSetting<E>(name, desc), Saving where E : Number, E : Comparable<E> {

    override var value: E = default
        set(value) {
            field = roundToIncrement(value).coerceIn(minDouble, maxDouble) as E
        }

    private val incrementDouble = increment.toDouble()
    private val minDouble = min.toDouble()
    private var maxDouble = max.toDouble()

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

    private val sliderAnim = LinearAnimation<Float>(100)
    private val handler = HoverHandler(0, 150)

    private var displayValue = getDisplay()
    private var prevLocation = 0f
    private var valueWidth = -1f

    private var sliderPercentage = ((valueDouble - minDouble) / (maxDouble - minDouble)).toFloat()
        set(value) {
            if (sliderPercentage != value) {
                prevLocation = sliderAnim.get(prevLocation, sliderPercentage, false)
                sliderAnim.start()
                displayValue = getDisplay()
                valueWidth = NVGRenderer.textWidth(displayValue, 16f, NVGRenderer.defaultFont)
            }
            field = value
        }

    override fun render(x: Float, y: Float, mouseX: Float, mouseY: Float): Float {
        super.render(x, y, mouseX, mouseY)
        if (valueWidth < 0) {
            sliderPercentage = ((valueDouble - minDouble) / (maxDouble - minDouble)).toFloat()
            valueWidth = NVGRenderer.textWidth(displayValue, 16f, NVGRenderer.defaultFont)
        }
        val height = getHeight()

        handler.handle(x, y + height / 2, width, height / 2)

        if (listening) {
            val newPercentage = ((mouseX - (x + 6f)) / (width - 12f)).coerceIn(0f, 1f)
            valueDouble = minDouble + newPercentage * (maxDouble - minDouble)
            sliderPercentage = newPercentage
        }

        NVGRenderer.text(name, x + 6f, y + height / 2f - 15f, 16f, Colors.WHITE.rgba, NVGRenderer.defaultFont)
        NVGRenderer.text(displayValue, x + width - valueWidth - 4f, y + height / 2f - 15f, 16f, Colors.WHITE.rgba, NVGRenderer.defaultFont)

        NVGRenderer.rect(x + 6f, y + 24f, width - 12f, 8f, gray38.rgba, 3f)

        if (x + sliderPercentage * (width - 12f) > x + 6)
            NVGRenderer.rect(x + 6f, y + 24f, sliderAnim.get(prevLocation, sliderPercentage, false) * (width - 12f), 8f, ClickGUIModule.clickGUIColor.rgba, 3f)

        NVGRenderer.circle(x + 6f + sliderAnim.get(prevLocation, sliderPercentage, false) * (width - 12f), y + 28f, handler.anim.get(7f, 9f, !isHovered), Colors.WHITE.rgba)

        return height
    }

    override fun mouseClicked(mouseX: Float, mouseY: Float, mouseButton: Int): Boolean {
        return if (mouseButton != 0 || !isHovered) false
        else {
            listening = true
            true
        }
    }

    override fun mouseReleased(state: Int) {
        listening = false
    }

    override fun keyPressed(keyCode: Int): Boolean {
        if (!isHovered) return false

        val amount = when (keyCode) {
            Keyboard.KEY_RIGHT, Keyboard.KEY_EQUALS -> incrementDouble
            Keyboard.KEY_LEFT, Keyboard.KEY_MINUS -> -incrementDouble
            else -> return false
        }

        if (valueDouble !in minDouble..maxDouble) return false
        valueDouble = (valueDouble + amount).coerceIn(minDouble, maxDouble)
        sliderPercentage = ((valueDouble - minDouble) / (maxDouble - minDouble)).toFloat()
        return true
    }

    override val isHovered: Boolean get() =
        isAreaHovered(lastX, lastY + getHeight() / 2, width, getHeight() / 2)

    override fun getHeight(): Float = Panel.HEIGHT + 8f

    override fun write(): JsonElement = JsonPrimitive(value)

    override fun read(element: JsonElement?) {
        element?.asNumber?.let { value = it as E }
    }

    private fun roundToIncrement(x: Number): Double =
        round((x.toDouble() / incrementDouble)) * incrementDouble

    private fun getDisplay(): String =
        if (valueDouble - floor(valueDouble) == 0.0)
            "${(valueInt * 100.0).roundToInt() / 100}${unit}"
        else
            "${(valueDouble * 100.0).roundToInt() / 100.0}${unit}"
}