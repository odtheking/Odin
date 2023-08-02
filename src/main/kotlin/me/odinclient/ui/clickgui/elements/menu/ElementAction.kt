package me.odinclient.ui.clickgui.elements.menu

import me.odinclient.features.settings.impl.ActionSetting
import me.odinclient.ui.clickgui.elements.Element
import me.odinclient.ui.clickgui.elements.ElementType
import me.odinclient.ui.clickgui.elements.ModuleButton
import me.odinclient.utils.render.gui.MouseUtils
import me.odinclient.utils.render.gui.nvg.NVG

// I made this so it exists, but it currently does not look great so feel free to improve it!
class ElementAction(parent: ModuleButton, setting: ActionSetting) : Element<ActionSetting>(parent, setting, ElementType.ACTION) {
    override val isHovered: Boolean
        get() = MouseUtils.isAreaHovered(x + 20f, y, w - 40f, h - 10f)


    override fun draw(nvg: NVG) {
        /*
        nvg.nanoVG {
            drawRect(x, y, w, h, 1)
            drawRoundedRect(x + 20f, y, w - 40f, h - 10f, 5f,
                if (isHovered) ColorUtil.clickGUIColor.rgba else ColorUtil.buttonColor
            )
            drawCustomCenteredText(name, x + w / 2, y + 12f, 16f, Fonts.REGULAR)
        }

         */
    }

    override fun mouseClicked(mouseButton: Int): Boolean {
        if (mouseButton == 0 && isHovered) {
            setting.doAction()
            return true
        }
        return false
    }
}