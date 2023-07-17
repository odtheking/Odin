package me.odinclient.clickgui.elements.menu

import cc.polyfrost.oneconfig.utils.dsl.VG
import me.odinclient.clickgui.elements.Element
import me.odinclient.clickgui.elements.ElementType
import me.odinclient.clickgui.elements.ModuleButton
import me.odinclient.clickgui.util.ColorUtil
import me.odinclient.clickgui.util.ColorUtil.darker
import me.odinclient.clickgui.util.FontUtil.drawCustomText
import me.odinclient.clickgui.util.FontUtil.getStringWidth
import me.odinclient.clickgui.util.MouseUtils
import me.odinclient.features.settings.impl.ActionSetting
import me.odinclient.utils.render.HUDRenderUtils.startDraw

class ElementAction(parent: ModuleButton, setting: ActionSetting) :
    Element<ActionSetting>(parent, setting, ElementType.ACTION)  {

    private var clicked: Boolean = false

    private fun checkIsHovered(middleX: Float, displayNameWidth: Float): Boolean {
        return MouseUtils.isAreaHovered(middleX.toDouble(), y + 2.0, displayNameWidth.toDouble(), height - 3.0)
    }

    override fun renderElement(partialTicks: Float, vg: VG) {
        vg.startDraw(x, y, width, height) {
            val middleX = x + width / 2 - getStringWidth(vg, displayName)
            val displayNameWidth = getStringWidth(vg, displayName) * 2 + 3
            if (checkIsHovered(middleX, displayNameWidth)) {
                roundedRect(middleX, y + 2, displayNameWidth, height - 3, ColorUtil.clickGUIColor.darker().rgb, 5f, 5f)
                roundRectOutline(middleX, y + 2, displayNameWidth, height - 3, ColorUtil.clickGUIColor.darker().rgb, radius = 5f, 1.25f)
                vg.drawCustomText(displayName,  middleX + 1.5, y + 8.5)
            } else {
                drawShadow(middleX, y + 2, displayNameWidth, height - 3, blur = 10f, spread = 0.75f, radius = 5f)
                roundedRect(middleX, y + 2, displayNameWidth, height - 3, ColorUtil.clickableColor, 5f, 5f)
                vg.drawCustomText(displayName,  middleX + 1.5, y + 8.5)
            }
        }
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