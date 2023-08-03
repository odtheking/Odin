package me.odinclient.ui.clickgui.elements.menu

import cc.polyfrost.oneconfig.renderer.font.Fonts
import me.odinclient.features.settings.impl.ActionSetting
import me.odinclient.ui.clickgui.elements.Element
import me.odinclient.ui.clickgui.elements.ElementType
import me.odinclient.ui.clickgui.elements.ModuleButton
import me.odinclient.ui.clickgui.util.ColorUtil.darker
import me.odinclient.ui.clickgui.util.ColorUtil.elementBackground
import me.odinclient.ui.clickgui.util.ColorUtil.textColor
import me.odinclient.utils.render.gui.MouseUtils
import me.odinclient.utils.render.gui.nvg.NVG
import me.odinclient.utils.render.gui.nvg.TextAlign
import me.odinclient.utils.render.gui.nvg.rect
import me.odinclient.utils.render.gui.nvg.text

class ElementAction(parent: ModuleButton, setting: ActionSetting) : Element<ActionSetting>(parent, setting, ElementType.ACTION) {
    override val isHovered: Boolean
        get() = MouseUtils.isAreaHovered(x + 20f, y, w - 40f, h - 10f)

// todo: improve this
    override fun draw(nvg: NVG) {
        nvg {
            rect(x, y, w, h, elementBackground)
            text(name, x + w / 2f, y + h / 2f, if (isHovered) textColor.darker() else textColor, 16f , Fonts.REGULAR, TextAlign.Middle)
        }
    }

    override fun mouseClicked(mouseButton: Int): Boolean {
        if (mouseButton == 0 && isHovered) {
            setting.doAction()
            return true
        }
        return false
    }
}