package me.odinclient.ui.clickgui.elements.menu

import cc.polyfrost.oneconfig.utils.dsl.VG
import me.odinclient.ui.clickgui.elements.Element
import me.odinclient.ui.clickgui.elements.ElementType
import me.odinclient.ui.clickgui.elements.ModuleButton
import me.odinclient.utils.gui.MouseUtils
import me.odinclient.features.settings.impl.ActionSetting

class ElementAction(parent: ModuleButton, setting: ActionSetting) :
    Element<ActionSetting>(parent, setting, ElementType.ACTION)  {

    private var clicked: Boolean = false

    private fun checkIsHovered(middleX: Float, displayNameWidth: Float): Boolean {
        return MouseUtils.isAreaHovered(middleX, y + 2f, displayNameWidth, height - 3f)
    }

    override fun renderElement(vg: VG) {
        /*
        vg.startDraw(x, y, width, height) {
            val middleX = x + width / 2 - getStringWidth(vg, displayName)
            val displayNameWidth = getStringWidth(vg, displayName) * 2 + 3
            if (checkIsHovered(middleX, displayNameWidth)) {
                roundedRect(middleX, y + 2, displayNameWidth, height - 3, ColorUtil.clickGUIColor.darker().rgb, 5f, 5f)
                roundRectOutline(middleX, y + 2, displayNameWidth, height - 3, ColorUtil.clickGUIColor.darker().rgb, radius = 5f, 1.25f)
                vg.drawText(displayName,  middleX + 1.5, y + 8.5, -1, 16f, Fonts.REGULAR)
            } else {
                drawShadow(middleX, y + 2, displayNameWidth, height - 3, blur = 10f, spread = 0.75f, radius = 5f)
                roundedRect(middleX, y + 2, displayNameWidth, height - 3, ColorUtil.clickableColor, 5f, 5f)
                vg.drawText(displayName,  middleX + 1.5, y + 8.5, -1, 16f, Fonts.REGULAR)
            }
        }

         */
    }

    override fun mouseClicked(mouseButton: Int): Boolean {
        if (mouseButton == 0 && isHovered) {
            (setting as? ActionSetting)?.doAction()
            clicked = true
            return true
        }
        return super.mouseClicked(mouseButton)
    }
}