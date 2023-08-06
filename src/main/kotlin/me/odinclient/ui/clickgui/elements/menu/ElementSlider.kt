package me.odinclient.ui.clickgui.elements.menu

import cc.polyfrost.oneconfig.renderer.font.Fonts
import me.odinclient.features.settings.impl.NumberSetting
import me.odinclient.ui.clickgui.elements.Element
import me.odinclient.ui.clickgui.elements.ElementType
import me.odinclient.ui.clickgui.elements.ModuleButton
import me.odinclient.ui.clickgui.util.ColorUtil.brighter
import me.odinclient.ui.clickgui.util.ColorUtil.clickGUIColor
import me.odinclient.ui.clickgui.util.ColorUtil.elementBackground
import me.odinclient.ui.clickgui.util.ColorUtil.textColor
import me.odinclient.ui.clickgui.util.HoverHandler
import me.odinclient.utils.Utils.coerceInNumber
import me.odinclient.utils.Utils.div
import me.odinclient.utils.Utils.minus
import me.odinclient.utils.Utils.plus
import me.odinclient.utils.Utils.times
import me.odinclient.utils.render.Color
import me.odinclient.utils.render.gui.MouseUtils.isAreaHovered
import me.odinclient.utils.render.gui.MouseUtils.mouseX
import me.odinclient.utils.render.gui.nvg.*
import org.lwjgl.input.Keyboard
import kotlin.math.roundToInt

class ElementSlider(parent: ModuleButton, setting: NumberSetting<*>) :
    Element<NumberSetting<*>>(parent, setting, ElementType.SLIDER) {

    private val displayVal: String
        get() = "${(setting.valueAsDouble * 100.0).roundToInt() / 100.0}"

    override val isHovered: Boolean
        get() = isAreaHovered(x, y, w - 12f, h)

    private val sliderBGColor = Color(-0xefeff0)

    private val handler = HoverHandler(0, 150)

    /** Used to make slider smoother and not jittery (doesn't change value.) */
    private var sliderPercentage: Float = ((setting.value - setting.min) / (setting.max - setting.min)).toFloat()

    private inline val color: Color
        get() = clickGUIColor.brighter(1 + handler.percent() / 200f)

    override fun draw(nvg: NVG) {
        handler.handle(x, y, w - 12f, h)
        val percentage = ((setting.value - setting.min) / (setting.max - setting.min)).toFloat()

        if (listening) {
            sliderPercentage = ((mouseX - (x + 6f)) / (w - 12f)).coerceIn(0f, 1f)
            println(sliderPercentage)
            val diff = setting.max - setting.min
            val newVal = setting.min + ((mouseX - (x + 6f)) / (w - 12f)).coerceInNumber(0, 1) * diff
            setting.valueAsDouble = newVal.toDouble()
        }

        nvg {
            rect(x, y, w, h, elementBackground)

            text(name, x + 6f, y + h / 2f - 3f, textColor, 16f, Fonts.REGULAR)
            text(displayVal, x + w - 6f, y + h / 2f - 3f, textColor, 16f, Fonts.REGULAR, TextAlign.Right)

            rect(x + 6f, y + 28f, w - 12f, 7f, sliderBGColor, 2.5f, 2.5f, 2.5f, 3f)
            dropShadow(x + 6f, y + 28f, w - 12f, 7f, 10f, 0.75f, 3f)
            if (x + percentage * (w - 12f) > x + 6) rect(x + 6f, y + 28f, sliderPercentage * (w - 12f), 7f, color, 3f)
        }
    }

    override fun mouseClicked(mouseButton: Int): Boolean {
        if (mouseButton == 0 && isHovered) {
            listening = true
            return true
        }
        return false
    }

    override fun mouseReleased(state: Int) {
        listening = false
    }

    override fun keyTyped(typedChar: Char, keyCode: Int): Boolean {
        if (isHovered) {
            val amount = when (keyCode) {
                Keyboard.KEY_RIGHT -> setting.increment.toDouble()
                Keyboard.KEY_LEFT -> -setting.increment.toDouble()
                else -> return false
            }
            setting.valueAsDouble += amount
            return true
        }
        return false
    }
}