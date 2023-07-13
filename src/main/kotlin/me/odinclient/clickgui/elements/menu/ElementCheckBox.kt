package me.odinclient.clickgui.elements.menu

import cc.polyfrost.oneconfig.utils.dsl.VG
import me.odinclient.features.settings.impl.BooleanSetting
import me.odinclient.clickgui.elements.Element
import me.odinclient.clickgui.elements.ElementType
import me.odinclient.clickgui.elements.ModuleButton
import me.odinclient.clickgui.util.ColorUtil
import me.odinclient.clickgui.util.FontUtil.drawCustomText
import me.odinclient.clickgui.util.MouseUtils.isAreaHovered
import me.odinclient.utils.render.HUDRenderUtils.startDraw

class ElementCheckBox(parent: ModuleButton, setting: BooleanSetting) : Element<BooleanSetting>(
    parent, setting, ElementType.CHECK_BOX
) {
    private val isCheckHovered
        get() = isAreaHovered(x + (width - 15), y + 2, 11, 10)

    override fun renderElement(partialTicks: Float, vg: VG) {
        vg.startDraw(x + (width - 15), y + 2.5, 10.5, 10) {
            vg.drawCustomText(displayName, x + 3, y + 9)

            drawShadow(blur = 10f, spread = 0.75f, radius = 5f)
            roundedRect(color = ColorUtil.buttonColor(setting.enabled), top = 5f, bottom = 5f)
            roundRectOutline(color = ColorUtil.clickGUIColor.rgb, radius = 5f, thickness = 1.25f)

            if (isCheckHovered)
                roundRectOutline(color = ColorUtil.boxHoverColor, radius = 5f, thickness = 1.25f)
        }
    }

    override fun mouseClicked(mouseButton: Int): Boolean {
        if (mouseButton == 0 && isCheckHovered) {
            setting.toggle()
            return true
        }
        return super.mouseClicked(mouseButton)
    }
}