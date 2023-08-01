package me.odinclient.ui.clickgui.elements.menu

import cc.polyfrost.oneconfig.renderer.font.Fonts
import cc.polyfrost.oneconfig.utils.dsl.*
import me.odinclient.features.impl.general.ClickGUIModule
import me.odinclient.features.settings.impl.NumberSetting
import me.odinclient.ui.clickgui.elements.Element
import me.odinclient.ui.clickgui.elements.ElementType
import me.odinclient.ui.clickgui.elements.ModuleButton
import me.odinclient.ui.clickgui.util.ColorUtil
import me.odinclient.ui.clickgui.util.ColorUtil.elementBackground
import me.odinclient.ui.clickgui.util.ColorUtil.withAlpha
import me.odinclient.utils.Utils.coerceInNumber
import me.odinclient.utils.Utils.compareTo
import me.odinclient.utils.Utils.div
import me.odinclient.utils.Utils.minus
import me.odinclient.utils.Utils.plus
import me.odinclient.utils.Utils.times
import me.odinclient.utils.render.gui.GuiUtils.nanoVG
import me.odinclient.utils.render.gui.MouseUtils.isAreaHovered
import me.odinclient.utils.render.gui.MouseUtils.mouseX
import org.lwjgl.input.Keyboard
import kotlin.math.roundToInt

class ElementSlider(parent: ModuleButton, setting: NumberSetting<*>) :
    Element<NumberSetting<*>>(parent, setting, ElementType.SLIDER) {

    private val displayVal get() = "${(setting.valueAsDouble * 100.0).roundToInt() / 100.0}"

    override fun draw(vg: VG) {
        val hoveredOrDragged = isHovered || listening
        val percentBar = (setting.value - setting.min) / (setting.max - setting.min)

        vg.nanoVG {
            drawRect(x, y, width, height, elementBackground)
            val textWidth = getTextWidth(displayVal, 16f, Fonts.REGULAR)
            drawText(displayName, x + 6f, y + height / 2f - 3f, -1, 16f, Fonts.REGULAR)
            drawText(displayVal, x + width - textWidth - 6f, y + height / 2f - 3f, -1, 16f, Fonts.REGULAR)

            drawRoundedRect(x + 6f, y + 27f, width - 12f, 6f, 2.5f, ColorUtil.sliderBackgroundColor)
            drawDropShadow(x + 6f, y + 27f, width - 12f, 6f, 10F, 0.75f, 5f)

            if (x + percentBar * (width - 12f) > x + 6) {
                drawRoundedRect(
                    x + 6f, y + 27f, percentBar * (width - 12f), 6f, 2.5f,
                    ClickGUIModule.color.withAlpha(if (hoveredOrDragged) 250 else 200).rgba,
                )
            }
        }

        if (listening) {
            val diff = setting.max - setting.min
            val newVal = setting.min + ((mouseX - (x + 6f)) / (width - 12f)).coerceInNumber(0, 1) * diff
            setting.valueAsDouble = newVal.toDouble()
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

    override val isHovered: Boolean get() = isAreaHovered(x, y, width - 12f, height)
}