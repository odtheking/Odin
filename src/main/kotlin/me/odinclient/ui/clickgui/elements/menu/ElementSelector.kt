package me.odinclient.ui.clickgui.elements.menu

import cc.polyfrost.oneconfig.renderer.font.Fonts
import cc.polyfrost.oneconfig.utils.dsl.*
import me.odinclient.ui.clickgui.elements.Element
import me.odinclient.ui.clickgui.elements.ElementType
import me.odinclient.ui.clickgui.elements.ModuleButton
import me.odinclient.ui.clickgui.util.ColorUtil
import me.odinclient.ui.clickgui.util.ColorUtil.buttonColor
import me.odinclient.ui.clickgui.util.ColorUtil.clickGUIColor
import me.odinclient.ui.clickgui.util.ColorUtil.elementBackground
import me.odinclient.ui.clickgui.util.FontUtil.drawCustomCenteredText
import me.odinclient.ui.clickgui.util.MouseUtils.isAreaHovered
import me.odinclient.features.settings.impl.SelectorSetting
import me.odinclient.utils.gui.GuiUtils.nanoVG
import me.odinclient.utils.gui.GuiUtils.resetScissor
import me.odinclient.utils.gui.GuiUtils.scissor
import me.odinclient.utils.gui.animations.EaseInOut
import java.util.*

class ElementSelector(parent: ModuleButton, setting: SelectorSetting) : Element<SelectorSetting>(parent, setting, ElementType.SELECTOR) {

    private val settingAnim = EaseInOut(200)

    private val isButtonHovered get() = isAreaHovered(x, y, width, DEFAULT_HEIGHT)
    private fun isSelectorHovered(currentY: Float) = isAreaHovered(x, currentY, width, 32f)

    override fun renderElement(vg: VG) {
        height = settingAnim.get(32f, setting.options.size * 36f + DEFAULT_HEIGHT, !extended)
        val displayValue = setting.selected

        vg.nanoVG {
            drawRect(x, y, width, height, elementBackground)
            val length = getTextWidth(displayValue, 16f, Fonts.REGULAR)
            drawDropShadow(x + width - 20f - length, y + 4f, length + 12f, 22f, 10f, 0.75f, 5f)
            drawRoundedRect(x + width - 20f - length, y + 4f, length + 12f, 22f, 5f, buttonColor)
            if (isButtonHovered) drawHollowRoundedRect(x + width - 21f - length, y + 3f, length + 12.5f, 22.5f, 4f, clickGUIColor.rgb, 1.5f)

            drawText(displayName, x + 6f, y + DEFAULT_HEIGHT / 2f, -1, 16f, Fonts.REGULAR)
            drawText(displayValue, x + width - 14f - length, y + DEFAULT_HEIGHT / 2f, -1, 16f, Fonts.REGULAR)

            if (extended || settingAnim.isAnimating()) {
                val scissor = scissor(x, y, width, height)
                drawRoundedRect(x + 6, y + 37f, width - 12f, setting.options.size * 32f, 5f, buttonColor)
                drawDropShadow(x + 6, y + 37f, width - 12f, setting.options.size * 32f, 10f, 0.75f, 5f)

                var currentY = y + 38
                for (option in setting.options) {

                    val elementTitle = option.substring(0, 1).uppercase(Locale.getDefault()) + option.substring(1, option.length)
                    drawCustomCenteredText(elementTitle, x + width / 2f, currentY + 16)
                    if (isSelectorHovered(currentY)) drawHollowRoundedRect(x + 5, currentY - 1f, width - 11.5f, 32.5f, 4f, ColorUtil.outlineColor, 1.5f)
                    currentY += 32
                }
                resetScissor(scissor)
            }
        }
    }

    override fun mouseClicked(mouseButton: Int): Boolean {
        if (mouseButton == 0) {
            if (isButtonHovered) {
                if (settingAnim.start()) extended = !extended
                return true
            }

            if (!extended) return false
            var currentY = 38
            for (option in setting.options) {
                if (isSelectorHovered(currentY + y)) {
                    setting.selected = option
                    if (settingAnim.start()) extended = false
                    return true
                }
                currentY += 32
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