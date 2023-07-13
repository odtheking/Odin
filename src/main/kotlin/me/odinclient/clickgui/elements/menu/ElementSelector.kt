package me.odinclient.clickgui.elements.menu

import cc.polyfrost.oneconfig.renderer.font.Fonts
import cc.polyfrost.oneconfig.utils.dsl.VG
import me.odinclient.clickgui.elements.Element
import me.odinclient.clickgui.elements.ElementType
import me.odinclient.clickgui.elements.ModuleButton
import me.odinclient.clickgui.util.ColorUtil
import me.odinclient.clickgui.util.ColorUtil.buttonColor
import me.odinclient.clickgui.util.FontUtil
import me.odinclient.clickgui.util.FontUtil.drawCustomCenteredText
import me.odinclient.clickgui.util.FontUtil.drawCustomText
import me.odinclient.clickgui.util.MouseUtils.isAreaHovered
import me.odinclient.features.settings.impl.SelectorSetting
import me.odinclient.utils.render.HUDRenderUtils.startDraw
import java.util.*

class ElementSelector(parent: ModuleButton, setting: SelectorSetting) : Element<SelectorSetting>(parent, setting, ElementType.SELECTOR) {

    private val isButtonHovered
        get() = isAreaHovered(x, y, width, DEFAULT_HEIGHT)

    private fun isSelectorHovered(currentY: Int) = // has to be a function for variable
        isAreaHovered(x, currentY, width, 16)

    override fun renderElement(partialTicks: Float, vg: VG) {
        val displayValue = setting.selected
        val length = FontUtil.getStringWidth(vg, displayValue, 16f, Fonts.REGULAR) * 2f

        vg.startDraw(x + (width - 10 - length), y + 2, length + 6, 11) {
            drawShadow()
            roundedRect(buttonColor)

            if (isButtonHovered)
                roundRectOutline(ColorUtil.clickGUIColor.rgb, radius = 5f, thickness = 1.25f)

            vg.drawCustomText(displayName, x + 3, y + 8.5)
            vg.drawCustomText(displayValue, x + (width - 7 - length), y + 8)

            if (extended) {
                roundedRect(x + 3, y + 18.5, width - 6, setting.options.size * 16, buttonColor, 5f, 5f)
                drawShadow(x + 3, y + 18.5, width - 6, setting.options.size * 16, 10f, 0.75f, 5f)

                var currentY = y + 19
                for (option in setting.options) {

                    val elementTitle = option.substring(0, 1).uppercase(Locale.getDefault()) + option.substring(1, option.length)
                    vg.drawCustomCenteredText(elementTitle, x + width / 2.0, currentY + 16)

                    if (isSelectorHovered(currentY)) {
                        roundRectOutline(x + 3, currentY, width - 6, 16, ColorUtil.outlineColor, 5f, 1f)
                    }
                    currentY += 16
                }
            }
        }
    }

    override fun mouseClicked(mouseButton: Int): Boolean {
        if (mouseButton == 0) {
            if (isButtonHovered) {
                extended = !extended
                return true
            }

            if (!extended) return false
            var currentY = 19
            for (option in setting.options) {
                if (isSelectorHovered(currentY + y)) {
                    setting.selected = option
                    extended = false
                    return true
                }
                currentY += 16
            }
        } else if (mouseButton == 1) {
            if (isButtonHovered) {
                setting.index += 1
                return true
            }
        }
        return super.mouseClicked(mouseButton)
    }
}