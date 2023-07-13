package me.odinclient.clickgui.elements.menu

import cc.polyfrost.oneconfig.renderer.font.Fonts
import cc.polyfrost.oneconfig.utils.dsl.VG
import me.odinclient.clickgui.elements.Element
import me.odinclient.clickgui.elements.ElementType
import me.odinclient.clickgui.elements.ModuleButton
import me.odinclient.clickgui.util.ColorUtil
import me.odinclient.clickgui.util.FontUtil
import me.odinclient.clickgui.util.FontUtil.drawCustomText
import me.odinclient.clickgui.util.MouseUtils.isAreaHovered
import me.odinclient.clickgui.util.MouseUtils.scaledMouseX
import me.odinclient.features.settings.impl.NumberSetting
import me.odinclient.utils.render.HUDRenderUtils.startDraw
import net.minecraft.util.MathHelper
import org.lwjgl.input.Keyboard
import kotlin.math.roundToInt

class ElementSlider(parent: ModuleButton, setting: NumberSetting) :
    Element<NumberSetting>(parent, setting, ElementType.SLIDER) {

    override fun renderElement(partialTicks: Float, vg: VG) {
        val displayVal = "${(setting.value * 100.0).roundToInt() / 100.0}"
        val hoveredOrDragged = isSliderHovered || listening
        val percentBar = (setting.value - setting.min) / (setting.max - setting.min)

        vg.startDraw(x + 3, y + 13, width - 6, 3) {

            val textWidth = FontUtil.getStringWidth(vg, displayVal, 16f, Fonts.REGULAR) * 2f
            vg.drawCustomText(displayName, x + 3, y + 8)
            vg.drawCustomText(displayVal, x + (width - textWidth - 3), y + 8)

            roundedRect(color = ColorUtil.sliderBackgroundColor, top = 2.5f, bottom = 2.5f)
            drawShadow(blur = 10f, spread = 0.75f, radius = 5f)

            if (x + 3 < x + percentBar * (width - 6))
                roundedRect(x + 3, y + 13, (percentBar * (width - 6)), 3, ColorUtil.sliderColor(hoveredOrDragged), 2.5f, 2.5f)
        }

        if (listening) {
            val diff = setting.max - setting.min
            val newVal = setting.min + MathHelper.clamp_double(((scaledMouseX - x) / width.toDouble()), 0.0, 1.0) * diff
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
        if (isSliderHovered)
            when (keyCode) {
                Keyboard.KEY_RIGHT -> {
                    setting.value += setting.increment
                    return true
                }
                Keyboard.KEY_LEFT -> {
                    setting.value -= setting.increment
                    return true
                }
            }
        return super.keyTyped(typedChar, keyCode)
    }

    private val isSliderHovered
        get() = isAreaHovered(x, y, width - 6, height)
}