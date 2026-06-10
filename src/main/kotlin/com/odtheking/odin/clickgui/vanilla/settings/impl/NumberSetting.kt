package com.odtheking.odin.clickgui.vanilla.settings.impl

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.clickgui.settings.ClickGUI.gray38
import com.odtheking.odin.clickgui.settings.Saving
import com.odtheking.odin.clickgui.vanilla.Panel
import com.odtheking.odin.clickgui.vanilla.VanillaRenderableSetting
import com.odtheking.odin.features.impl.render.ClickGUIModule
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.render.DrawContextRenderer.drawCircle
import com.odtheking.odin.utils.render.roundedFill
import com.odtheking.odin.utils.ui.animations.LinearAnimation
import com.odtheking.odin.utils.ui.isAreaHovered
import net.minecraft.client.gui.GuiGraphics
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
    min: Number,
    max: Number,
    increment: Number = 1,
    desc: String,
    private val unit: String = ""
) : VanillaRenderableSetting<E>(name, desc), Saving where E : Number, E : Comparable<E> {

    private val incrementDouble = increment.toDouble()
    private val minDouble       = min.toDouble()
    private val maxDouble       = max.toDouble()

    private val sliderAnim  = LinearAnimation<Float>(100)
    private var prevLocation = 0f

    private val hoverAnim = LinearAnimation<Float>(150)
    private var wasHovered = false

    private var isDragging  = false

    private var displayValue = ""

    private var sliderPercentage = 0f
        set(value) {
            if (field != value) {
                if (!isDragging) {
                    prevLocation = sliderAnim.get(prevLocation, field, false)
                    sliderAnim.start()
                }
                displayValue = getDisplay()
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
        set(v) { value = v as E }

    override fun render(graphics: GuiGraphics, x: Int, y: Int, mouseX: Int, mouseY: Int): Int {
        super.render(graphics, x, y, mouseX, mouseY)
        val height = getHeight()

        if (isHovered != wasHovered) { hoverAnim.start(); wasHovered = isHovered }

        if (listening) {
            val pct = ((mouseX - (x + 6f)) / (width - 12f)).coerceIn(0f, 1f)
            valueDouble = minDouble + pct * (maxDouble - minDouble)
            sliderPercentage = pct
        }

        val labelY = y + height / 2 - 8

        graphics.drawString(mc.font, name, x + 6, labelY, Colors.WHITE.rgba, false)
        val valueWidth = mc.font.width(displayValue)
        graphics.drawString(mc.font, displayValue, x + width - valueWidth - 4, labelY, Colors.WHITE.rgba, false)

        val trackX = x + 6
        val trackY = y + 18
        val trackW = width - 12
        graphics.roundedFill(trackX, trackY, trackX + trackW, trackY + 6, gray38.rgba, 3)

        val animPct = sliderAnim.get(prevLocation, sliderPercentage, false)
        val fillW   = (animPct * trackW).toInt()
        if (fillW > 0)
            graphics.roundedFill(trackX, trackY, trackX + fillW, trackY + 6, ClickGUIModule.clickGUIColor.rgba, 3)

        val knobCx = trackX + fillW
        val knobCy = trackY + 3
        val knobR  = hoverAnim.get(4f, 5f, !isHovered).toInt()
        graphics.drawCircle(knobCx, knobCy, knobR, Colors.WHITE.rgba)

        return height
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, click: MouseButtonEvent): Boolean {
        if (click.button() != 0 || !isHovered) return false
        listening  = true
        isDragging = true
        prevLocation = sliderPercentage
        sliderAnim.start()
        return true
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, click: MouseButtonEvent) {
        listening = false
        if (isDragging) {
            isDragging = false
            prevLocation = sliderAnim.get(prevLocation, sliderPercentage, false)
            sliderAnim.start()
        }
    }

    override fun keyPressed(input: KeyEvent): Boolean {
        if (!isHovered) return false
        val delta = when (input.key) {
            GLFW.GLFW_KEY_RIGHT, GLFW.GLFW_KEY_EQUAL -> incrementDouble
            GLFW.GLFW_KEY_LEFT,  GLFW.GLFW_KEY_MINUS -> -incrementDouble
            else -> return false
        }
        valueDouble = (valueDouble + delta).coerceIn(minDouble, maxDouble)
        return true
    }

    override val isHovered: Boolean
        get() =
            isAreaHovered(lastX, lastY + getHeight() / 2, width, getHeight() / 2)

    override fun getHeight(): Int = Panel.HEIGHT + 8

    override fun write(gson: Gson): JsonElement = JsonPrimitive(value)

    override fun read(element: JsonElement, gson: Gson) {
        element.asNumber?.let { value = it as E }
    }

    private fun roundToIncrement(x: Number): Double =
        round(x.toDouble() / incrementDouble) * incrementDouble

    private fun getDisplay(): String {
        val v = value.toDouble()
        return if (v % 1.0 == 0.0) "${v.toInt()}$unit"
        else "${(v * 100).roundToInt() / 100.0}$unit"
    }
}