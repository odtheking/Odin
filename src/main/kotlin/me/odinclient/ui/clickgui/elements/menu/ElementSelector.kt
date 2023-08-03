package me.odinclient.ui.clickgui.elements.menu

import cc.polyfrost.oneconfig.renderer.font.Fonts
import me.odinclient.features.settings.impl.SelectorSetting
import me.odinclient.ui.clickgui.elements.Element
import me.odinclient.ui.clickgui.elements.ElementType
import me.odinclient.ui.clickgui.elements.ModuleButton
import me.odinclient.ui.clickgui.util.ColorUtil.brighter
import me.odinclient.ui.clickgui.util.ColorUtil.buttonColor
import me.odinclient.ui.clickgui.util.ColorUtil.clickGUIColor
import me.odinclient.ui.clickgui.util.ColorUtil.darker
import me.odinclient.ui.clickgui.util.ColorUtil.elementBackground
import me.odinclient.ui.clickgui.util.ColorUtil.textColor
import me.odinclient.ui.clickgui.util.HoverHandler
import me.odinclient.utils.render.Color
import me.odinclient.utils.render.gui.GuiUtils.capitalizeFirst
import me.odinclient.utils.render.gui.MouseUtils.isAreaHovered
import me.odinclient.utils.render.gui.animations.impl.EaseInOut
import me.odinclient.utils.render.gui.nvg.*

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

    override fun draw(nvg: NVG) {
        h = settingAnim.get(32f, size * 36f + DEFAULT_HEIGHT, !extended)

        nvg {
            rect(x, y, w, h, elementBackground)
            val width = getTextWidth(display, 16f, Fonts.REGULAR)

            hover.handle(x + w - 20f - width, y + 4f, width + 12f, 22f)
            dropShadow(x + w - 20f - width, y + 4f, width + 12f, 22f, 10f, 0.75f, 5f)
            rect(x + w - 20f - width, y + 4f, width + 12f, 22f, color, 5f)


            text(name, x + 6f, y + 16f, textColor, 16f, Fonts.REGULAR)
            text(display, x + w - 14f - width, y + 16f, textColor, 16f, Fonts.REGULAR)

            if (!extended && !settingAnim.isAnimating()) return@nvg

            rectOutline(x + w - 20f - width, y + 4f, width + 12f, 22f, clickGUIColor, 5f, 1.5f)

            val scissor = scissor(x, y, w, h)

            rect(x + 6, y + 37f, w - 12f, size * 32f, buttonColor, 5f)
            dropShadow(x + 6, y + 37f, w - 12f, size * 32f, 10f, 0.75f, 5f)

            for (i in 0 until size) {
                val y = y + 38 + 32 * i
                text(setting.options[i].capitalizeFirst(), x + w / 2f, y + 16f, textColor, 16f, Fonts.REGULAR, TextAlign.Middle)
                text(setting.options[i].capitalizeFirst(), x + w / 2f, y + 16f, textColor, 16f, Fonts.REGULAR, TextAlign.Middle)
                if (isSettingHovered(i)) rectOutline(x + 5, y - 1f, w - 11.5f, 32.5f, clickGUIColor.darker(), 4f, 1.5f)
            }
            resetScissor(scissor)
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