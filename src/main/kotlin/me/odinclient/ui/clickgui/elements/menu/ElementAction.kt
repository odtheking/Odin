package me.odinclient.ui.clickgui.elements.menu

import cc.polyfrost.oneconfig.renderer.font.Fonts
import cc.polyfrost.oneconfig.utils.dsl.*
import me.odinclient.features.settings.impl.ActionSetting
import me.odinclient.ui.clickgui.elements.Element
import me.odinclient.ui.clickgui.elements.ElementType
import me.odinclient.ui.clickgui.elements.ModuleButton
import me.odinclient.ui.clickgui.util.ColorUtil
import me.odinclient.ui.clickgui.util.ColorUtil.darker
import me.odinclient.utils.render.gui.GuiUtils.drawCustomCenteredText
import me.odinclient.utils.render.gui.GuiUtils.drawOutlineRoundedRect
import me.odinclient.utils.render.gui.GuiUtils.nanoVG
import me.odinclient.utils.render.gui.MouseUtils

// I made this so it exists, but it currently does not look great so feel free to improve it!
class ElementAction(parent: ModuleButton, setting: ActionSetting) : Element<ActionSetting>(parent, setting, ElementType.ACTION) {
    override val isHovered: Boolean
        get() = MouseUtils.isAreaHovered(x + 20f, y, width - 40f, height - 10f)


    override fun draw(vg: VG) {
        vg.nanoVG {
            drawRect(x, y, width, height, ColorUtil.elementBackground)
            drawRoundedRect(x + 20f, y, width - 40f, height - 10f, 5f,
                if (isHovered) ColorUtil.clickGUIColor.rgba else ColorUtil.buttonColor
            )
            drawCustomCenteredText(displayName, x + width / 2, y + 12f, 16f, Fonts.REGULAR)
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