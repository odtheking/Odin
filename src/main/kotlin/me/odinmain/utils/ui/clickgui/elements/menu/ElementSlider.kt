package me.odinmain.utils.ui.clickgui.elements.menu

import me.odinmain.features.settings.impl.NumberSetting
import me.odinmain.font.OdinFont
import me.odinmain.utils.render.*
import me.odinmain.utils.ui.clickgui.elements.Element
import me.odinmain.utils.ui.clickgui.elements.ElementType
import me.odinmain.utils.ui.clickgui.elements.ModuleButton
import me.odinmain.utils.ui.clickgui.util.ColorUtil.brighter
import me.odinmain.utils.ui.clickgui.util.ColorUtil.clickGUIColor
import me.odinmain.utils.ui.clickgui.util.ColorUtil.elementBackground
import me.odinmain.utils.ui.clickgui.util.ColorUtil.textColor
import me.odinmain.utils.ui.clickgui.util.HoverHandler
import me.odinmain.utils.ui.util.MouseUtils.isAreaHovered
import me.odinmain.utils.ui.util.MouseUtils.mouseX
import org.lwjgl.input.Keyboard
import kotlin.math.floor
import kotlin.math.roundToInt

/**
 * Renders all the modules.
 *
 * Backend made by Aton, with some changes
 * Design mostly made by Stivais
 *
 * @author Stivais, Aton
 * @see [Element]
 */
class ElementSlider(parent: ModuleButton, setting: NumberSetting<*>) :
    Element<NumberSetting<*>>(parent, setting, ElementType.SLIDER) {

    override val isHovered: Boolean
        get() = isAreaHovered(x, y, w - 12f, h)

    private val handler = HoverHandler(0, 150)

    /** Used to make slider smoother and not jittery (doesn't change value.) */
    private var sliderPercentage: Float = ((setting.valueDouble - setting.min) / (setting.max - setting.min)).toFloat()

    private inline val color: Color
        get() = clickGUIColor.brighter(1 + handler.percent() / 200f)

    private fun getDisplay(): String {
        return if (setting.valueDouble - floor(setting.valueDouble) == 0.0) {
            "${(setting.valueInt * 100.0).roundToInt() / 100}${setting.unit}"
        } else {
            "${(setting.valueDouble * 100.0).roundToInt() / 100.0}${setting.unit}"
        }
    }

    override fun draw() {
        handler.handle(x, y, w - 12f, h)
        val percentage = ((setting.valueDouble - setting.min) / (setting.max - setting.min)).toFloat()

        if (listening) {
            sliderPercentage = ((mouseX - (x + 6f)) / (w - 12f)).coerceIn(0f, 1f)
            val diff = setting.max - setting.min
            val newVal = setting.min + ((mouseX - (x + 6f)) / (w - 12f)).coerceIn(0f, 1f) * diff
            setting.valueDouble = newVal
        }
        roundedRectangle(x, y, w, h, elementBackground)

        text(name, x + 6f, y + h / 2f - 3f, textColor, 12f, OdinFont.REGULAR)
        text(getDisplay(), x + w - 6f, y + h / 2f - 3f, textColor, 12f, OdinFont.REGULAR, TextAlign.Right)

        roundedRectangle(x + 6f, y + 28f, w - 12f, 7f, sliderBGColor, 2.5f)
        dropShadow(x + 6f, y + 28f, w - 12f, 7f, 10f, 0.75f)
        if (x + percentage * (w - 12f) > x + 6) roundedRectangle(x + 6f, y + 28f, sliderPercentage * (w - 12f), 7f, color, 3f)
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
                Keyboard.KEY_RIGHT -> setting.increment
                Keyboard.KEY_LEFT -> -setting.increment
                else -> return false
            }
            setting.valueDouble += amount
            return true
        }
        return false
    }

    private companion object {
        val sliderBGColor = Color(-0xefeff0)
    }
}