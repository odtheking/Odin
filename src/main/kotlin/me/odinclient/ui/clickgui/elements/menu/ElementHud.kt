package me.odinclient.ui.clickgui.elements.menu

import cc.polyfrost.oneconfig.renderer.font.Fonts
import me.odinclient.features.settings.impl.HudSetting
import me.odinclient.ui.clickgui.elements.Element
import me.odinclient.ui.clickgui.elements.ElementType
import me.odinclient.ui.clickgui.elements.ModuleButton
import me.odinclient.ui.clickgui.util.ColorUtil
import me.odinclient.ui.clickgui.util.ColorUtil.brighterIf
import me.odinclient.ui.clickgui.util.ColorUtil.darker
import me.odinclient.utils.render.Color
import me.odinclient.utils.render.gui.MouseUtils
import me.odinclient.utils.render.gui.nvg.*

class ElementHud(parent: ModuleButton, setting: HudSetting) : Element<HudSetting>(
    parent, setting, ElementType.DUAL
) {
    override val isHovered: Boolean
        get() = MouseUtils.isAreaHovered(x + w - 41, y + 5, 31.5f, 19f)

    override fun draw(nvg: NVG) {
        nvg {
            rect(x, y, w, h, ColorUtil.elementBackground)
            text(name, x + 6f, y + 18f, Color.WHITE, 16f, Fonts.REGULAR)
            dropShadow(x + w - 40f, y + 5f, 31f, 19f, 10f, 0.75f, 5f)
            rect(x + w - 40f, y + 5f, 31f, 19f, ColorUtil.clickGUIColor.brighterIf(isHovered), 5f)
            rectOutline(x + w - 40f, y + 5f, 31f, 19f, ColorUtil.clickGUIColor.darker(.8f), 5f, 1.5f)
        }
    }

    override fun mouseClicked(mouseButton: Int): Boolean {
        if (mouseButton == 0 && isHovered) {
            setting.value.xSetting.hidden = !setting.value.xSetting.hidden
            setting.value.ySetting.hidden = !setting.value.ySetting.hidden
            setting.value.scaleSetting.hidden = !setting.value.scaleSetting.hidden
            parent.updateElements()
            return true
        }
        return false
    }
}