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
import me.odinclient.utils.gui.MouseUtils.isAreaHovered
import me.odinclient.features.settings.impl.SelectorSetting
import me.odinclient.utils.gui.GuiUtils.drawCustomCenteredText
import me.odinclient.utils.gui.GuiUtils.nanoVG
import me.odinclient.utils.gui.GuiUtils.scissor
import me.odinclient.utils.gui.animations.EaseInOut
import java.util.*

class ElementSelector(parent: ModuleButton, setting: SelectorSetting) : Element<SelectorSetting>(parent, setting, ElementType.SELECTOR) {

    private val settingAnim = EaseInOut(200)

    override val isHovered get() = isAreaHovered(x, y, width, DEFAULT_HEIGHT)
    private val isSettingHovered: (Int) -> Boolean = { isAreaHovered(x, y + 38f + 32f * it, width, 32f) }

    override fun renderElement(vg: VG) {
        height = settingAnim.get(32f, setting.options.size * 36f + DEFAULT_HEIGHT, !extended)
        val displayValue = setting.selected

        vg.nanoVG {
            drawRect(x, y, width, height, elementBackground)
            val length = getTextWidth(displayValue, 16f, Fonts.REGULAR)
            drawDropShadow(x + width - 20f - length, y + 4f, length + 12f, 22f, 10f, 0.75f, 5f)
            drawRoundedRect(x + width - 20f - length, y + 4f, length + 12f, 22f, 5f, buttonColor)
            if (isHovered) drawHollowRoundedRect(x + width - 21f - length, y + 3f, length + 12.5f, 22.5f, 4f, clickGUIColor.rgb, 1.5f)

            drawText(displayName, x + 6f, y + DEFAULT_HEIGHT / 2f, -1, 16f, Fonts.REGULAR)
            drawText(displayValue, x + width - 14f - length, y + DEFAULT_HEIGHT / 2f, -1, 16f, Fonts.REGULAR)

            if (extended || settingAnim.isAnimating()) {
                scissor(x, y, width, height) {
                    drawRoundedRect(x + 6, y + 37f, width - 12f, setting.options.size * 32f, 5f, buttonColor)
                    drawDropShadow(x + 6, y + 37f, width - 12f, setting.options.size * 32f, 10f, 0.75f, 5f)

                    for ((index, option) in setting.options.withIndex()) {
                        val y = y + 38 + 32 * index
                        val elementTitle = option.substring(0, 1).uppercase(Locale.getDefault()) + option.substring(1, option.length)
                        drawCustomCenteredText(elementTitle, x + width / 2f, y, 16f, Fonts.REGULAR)
                        if (isSettingHovered(index)) drawHollowRoundedRect(x + 5, y - 1f, width - 11.5f, 32.5f, 4f, ColorUtil.outlineColor, 1.5f)
                    }
                }
            }
        }
    }

    override fun mouseClicked(mouseButton: Int): Boolean {
        if (mouseButton == 0) {
            if (isHovered) {
                if (settingAnim.start()) extended = !extended
                return true
            }
            if (!extended) return false
            for (index in 0 until setting.options.size) {
                if (isSettingHovered(index)) {
                    if (settingAnim.start()) {
                        setting.selected = setting.options[index]
                        extended = false
                    }
                    return true
                }
            }
        } else if (mouseButton == 1) {
            if (isHovered) {
                setting.index += 1
                return true
            }
        }
        return false
    }
}