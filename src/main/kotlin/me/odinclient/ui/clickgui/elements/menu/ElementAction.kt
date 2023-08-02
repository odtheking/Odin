package me.odinclient.ui.clickgui.elements.menu

import cc.polyfrost.oneconfig.renderer.font.Fonts
import me.odinclient.features.settings.impl.ActionSetting
import me.odinclient.ui.clickgui.elements.Element
import me.odinclient.ui.clickgui.elements.ElementType
import me.odinclient.ui.clickgui.elements.ModuleButton
import me.odinclient.ui.clickgui.util.ColorUtil.elementBackground
import me.odinclient.ui.clickgui.util.ColorUtil.textColor
import me.odinclient.utils.render.gui.MouseUtils
import me.odinclient.utils.render.gui.nvg.NVG
import me.odinclient.utils.render.gui.nvg.TextAlign
import me.odinclient.utils.render.gui.nvg.rect
import me.odinclient.utils.render.gui.nvg.text

// I made this so it exists, but it currently does not look great so feel free to improve it!
class ElementAction(parent: ModuleButton, setting: ActionSetting) : Element<ActionSetting>(parent, setting, ElementType.ACTION) {
    override val isHovered: Boolean
        get() = MouseUtils.isAreaHovered(x + 20f, y, w - 40f, h - 10f)

// todo: make it good 1 day
    override fun draw(nvg: NVG) {
        nvg {
            rect(x, y, w, h, elementBackground)
            text(name, x + w / 2f, y + h / 2f, textColor, 16f , Fonts.REGULAR, TextAlign.Middle)
        }

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