package me.odinmain.ui.clickgui.elements.menu

import me.odinmain.features.settings.impl.ActionSetting
import me.odinmain.font.OdinFont
import me.odinmain.ui.clickgui.elements.*
import me.odinmain.ui.clickgui.util.ColorUtil.darker
import me.odinmain.ui.clickgui.util.ColorUtil.elementBackground
import me.odinmain.ui.clickgui.util.ColorUtil.textColor
import me.odinmain.ui.util.MouseUtils
import me.odinmain.utils.render.*

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
    override fun draw() {
        roundedRectangle(x, y, w, h, elementBackground)
        text(name, x + w / 2f, y + h / 2f, if (isHovered) textColor.darker() else textColor, 12f , OdinFont.REGULAR, TextAlign.Middle, TextPos.Middle)
    }

    override fun mouseClicked(mouseButton: Int): Boolean {
        if (mouseButton == 0 && isHovered) {
            setting.action()
            return true
        }
        return false
    }
}