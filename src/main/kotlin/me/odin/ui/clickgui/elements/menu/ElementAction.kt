package me.odin.ui.clickgui.elements.menu

import cc.polyfrost.oneconfig.renderer.font.Fonts
import me.odin.features.settings.impl.ActionSetting
import me.odin.ui.clickgui.elements.Element
import me.odin.ui.clickgui.elements.ElementType
import me.odin.ui.clickgui.elements.ModuleButton
import me.odin.ui.clickgui.util.ColorUtil.darker
import me.odin.ui.clickgui.util.ColorUtil.elementBackground
import me.odin.ui.clickgui.util.ColorUtil.textColor
import me.odin.utils.render.gui.MouseUtils
import me.odin.utils.render.gui.nvg.NVG
import me.odin.utils.render.gui.nvg.TextAlign
import me.odin.utils.render.gui.nvg.rect
import me.odin.utils.render.gui.nvg.text

/**
 * Renders all the modules.
 *
 * Backend made by Aton, with some changes
 * Design mostly made by Stivais
 *
 * @author Stivais, Aton
 * @see [Element]
 */
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