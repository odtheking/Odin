package me.odinclient.ui.clickgui.elements.menu

import cc.polyfrost.oneconfig.renderer.font.Fonts
import cc.polyfrost.oneconfig.utils.dsl.*
import me.odinclient.ui.clickgui.elements.Element
import me.odinclient.ui.clickgui.elements.ElementType
import me.odinclient.ui.clickgui.elements.ModuleButton
import me.odinclient.ui.clickgui.util.ColorUtil
import me.odinclient.ui.clickgui.util.ColorUtil.elementBackground
import me.odinclient.ui.clickgui.util.ColorUtil.withAlpha
import me.odinclient.utils.gui.MouseUtils.isAreaHovered
import me.odinclient.utils.gui.MouseUtils.mouseX
import me.odinclient.features.impl.general.ClickGUIModule
import me.odinclient.features.settings.impl.NumberSetting
import me.odinclient.utils.gui.GuiUtils.nanoVG
import org.lwjgl.input.Keyboard
import kotlin.math.roundToInt

class ElementSlider(parent: ModuleButton, setting: NumberSetting<*>) :
    Element<NumberSetting<*>>(parent, setting, ElementType.SLIDER) {

    private val displayVal get() = "${(setting.valueAsDouble * 100.0).roundToInt() / 100.0}"

    override fun draw(vg: VG) {
        val hoveredOrDragged = isHovered || listening
        val percentBar = (setting.valueAsDouble - setting.min) / (setting.max - setting.min)

        vg.nanoVG {
            drawRect(x, y, width, height, elementBackground)
            val textWidth = getTextWidth(displayVal, 16f, Fonts.REGULAR)
            drawText(displayName, x + 6f, y + height / 2f - 3f, -1, 16f, Fonts.REGULAR)
            drawText(displayVal, x + width - textWidth - 6f, y + height / 2f - 3f, -1, 16f, Fonts.REGULAR)

            drawRoundedRect(x + 6f, y + 26f, width - 12f, 6f, 2.5f, ColorUtil.sliderBackgroundColor)
            drawDropShadow(x + 6f, y + 26f, width - 12f, 6f, 10F, 0.75f, 5f)

            if (x + 6 < x + percentBar * (width - 12f)) {
                drawGradientRoundedRect(
                    x + 6f, y + 26f, (width - 12f) * percentBar, 6f,
                    ClickGUIModule.color.withAlpha(if (hoveredOrDragged) 250 else 200).rgb,
                    ClickGUIModule.secondColor.withAlpha(if (hoveredOrDragged) 250 else 200).rgb,
                    2.5f
                )
            }
        }

        if (listening) {
            val diff = setting.max - setting.min
            val newVal = setting.min + ((mouseX - x) / width).clamp(0, 1) * diff
            setting.valueAsDouble = newVal
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
                Keyboard.KEY_RIGHT -> setting.increment
                Keyboard.KEY_LEFT -> -setting.increment
                else -> return false
            }
            setting.valueAsDouble += amount
            return true
        }
        return false
    }

    override val isHovered: Boolean get() = isAreaHovered(x, y, width - 12f, height)
}