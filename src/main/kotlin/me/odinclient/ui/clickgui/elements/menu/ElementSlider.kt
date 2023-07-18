package me.odinclient.ui.clickgui.elements.menu

import cc.polyfrost.oneconfig.renderer.font.Fonts
import cc.polyfrost.oneconfig.utils.dsl.*
import me.odinclient.ui.clickgui.elements.Element
import me.odinclient.ui.clickgui.elements.ElementType
import me.odinclient.ui.clickgui.elements.ModuleButton
import me.odinclient.ui.clickgui.util.ColorUtil
import me.odinclient.ui.clickgui.util.ColorUtil.elementBackground
import me.odinclient.ui.clickgui.util.ColorUtil.withAlpha
import me.odinclient.ui.clickgui.util.MouseUtils.isAreaHovered
import me.odinclient.ui.clickgui.util.MouseUtils.mouseX
import me.odinclient.features.impl.general.ClickGUIModule
import me.odinclient.features.settings.impl.NumberSetting
import me.odinclient.utils.gui.GuiUtils.nanoVG
import net.minecraft.util.MathHelper
import org.lwjgl.input.Keyboard
import kotlin.math.roundToInt

class ElementSlider(parent: ModuleButton, setting: NumberSetting) :
    Element<NumberSetting>(parent, setting, ElementType.SLIDER) {

    override fun renderElement(vg: VG) {
        val displayVal = "${(setting.value * 100.0).roundToInt() / 100.0}"
        val hoveredOrDragged = isSliderHovered || listening
        val percentBar = (setting.value - setting.min) / (setting.max - setting.min)

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
            val newVal = setting.min + MathHelper.clamp_double(((mouseX - x) / width.toDouble()), 0.0, 1.0) * diff
            setting.value = newVal
        }
    }

    override fun mouseClicked(mouseButton: Int): Boolean {
        if (mouseButton == 0 && isSliderHovered) {
            listening = true
            return true
        }
        return super.mouseClicked(mouseButton)
    }

    override fun mouseReleased(state: Int) {
        listening = false
    }

    override fun keyTyped(typedChar: Char, keyCode: Int): Boolean {
        if (isSliderHovered) {
            val amount = when (keyCode) {
                Keyboard.KEY_RIGHT -> setting.increment
                Keyboard.KEY_LEFT -> -setting.increment
                else -> return super.keyTyped(typedChar, keyCode)
            }
            setting.value += amount
        }
        return super.keyTyped(typedChar, keyCode)
    }

    private val isSliderHovered get() = isAreaHovered(x, y, width - 12f, height)
}