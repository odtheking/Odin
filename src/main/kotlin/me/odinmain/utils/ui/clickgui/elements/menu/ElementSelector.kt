package me.odinmain.utils.ui.clickgui.elements.menu

import me.odinmain.features.settings.impl.SelectorSetting
import me.odinmain.font.OdinFont
import me.odinmain.utils.capitalizeFirst
import me.odinmain.utils.render.*
import me.odinmain.utils.ui.clickgui.animations.impl.EaseInOut
import me.odinmain.utils.ui.clickgui.elements.Element
import me.odinmain.utils.ui.clickgui.elements.ElementType
import me.odinmain.utils.ui.clickgui.elements.ModuleButton
import me.odinmain.utils.ui.clickgui.util.ColorUtil.brighter
import me.odinmain.utils.ui.clickgui.util.ColorUtil.buttonColor
import me.odinmain.utils.ui.clickgui.util.ColorUtil.clickGUIColor
import me.odinmain.utils.ui.clickgui.util.ColorUtil.darker
import me.odinmain.utils.ui.clickgui.util.ColorUtil.elementBackground
import me.odinmain.utils.ui.clickgui.util.ColorUtil.textColor
import me.odinmain.utils.ui.clickgui.util.HoverHandler
import me.odinmain.utils.ui.util.MouseUtils.isAreaHovered

/**
 * Renders all the modules.
 *
 * Backend made by Aton, with some changes
 * Design mostly made by Stivais
 *
 * @author Stivais, Aton
 * @see [Element]
 */
class ElementSelector(parent: ModuleButton, setting: SelectorSetting) :
    Element<SelectorSetting>(parent, setting, ElementType.SELECTOR) {

    override val isHovered: Boolean
        get() = isAreaHovered(x, y, w, DEFAULT_HEIGHT)

    val display: String
        inline get() = setting.selected

    inline val size: Int
        get () = setting.options.size

    private val settingAnim = EaseInOut(200)

    private val isSettingHovered: (Int) -> Boolean = {
        isAreaHovered(x, y + 38f + 32f * it, w, 32f)
    }

    private val hover = HoverHandler(0, 150)

    private val color: Color
        get() = buttonColor.brighter(1 + hover.percent() / 500f)

    override fun draw() {
        h = settingAnim.get(32f, size * 36f + DEFAULT_HEIGHT, !extended)

        roundedRectangle(x, y, w, h, elementBackground)
        val width = getTextWidth(display, 12f)

        hover.handle(x + w - 20f - width, y + 4f, width + 12f, 22f)
        dropShadow(x + w - 20f - width, y + 4f, width + 12f, 22f, 10f, 0.75f)
        roundedRectangle(x + w - 20f - width, y + 4f, width + 12f, 22f, color, 5f)

        text(name, x + 6f, y + 16f, textColor, 12f, OdinFont.REGULAR)
        text(display, x + w - 14f - width, y + 8f, textColor, 12f, OdinFont.REGULAR, TextAlign.Left, TextPos.Top)

        if (!extended && !settingAnim.isAnimating()) return

        rectangleOutline(x + w - 20f - width, y + 4f, width + 12f, 22f, clickGUIColor, 5f, 1.5f)

        val scissor = scissor(x, y, w, h)

        roundedRectangle(x + 6, y + 37f, w - 12f, size * 32f, buttonColor, 5f)
        dropShadow(x + 6, y + 37f, w - 12f, size * 32f, 10f, 0.75f)

        for (i in 0 until size) {
            val y = y + 38 + 32 * i
            text(setting.options[i].lowercase().capitalizeFirst(), x + w / 2f, y + 6f, textColor, 12f, OdinFont.REGULAR, TextAlign.Middle, TextPos.Top)
            if (isSettingHovered(i)) rectangleOutline(x + 5, y - 1f, w - 11.5f, 32.5f, clickGUIColor.darker(), 4f, 3f)
        }
        resetScissor(scissor)
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
                setting.index++
                return true
            }
        }
        return false
    }
}