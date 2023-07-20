package me.odinclient.ui.clickgui.elements.menu

import cc.polyfrost.oneconfig.renderer.font.Fonts
import cc.polyfrost.oneconfig.utils.dsl.*
import me.odinclient.ui.clickgui.elements.Element
import me.odinclient.ui.clickgui.elements.ElementType
import me.odinclient.ui.clickgui.elements.ModuleButton
import me.odinclient.ui.clickgui.util.ColorUtil
import me.odinclient.utils.gui.MouseUtils.isAreaHovered
import me.odinclient.features.settings.impl.BooleanSetting
import me.odinclient.utils.gui.GuiUtils.nanoVG
import me.odinclient.utils.gui.animations.ColorAnimation
import java.awt.Color

class ElementCheckBox(parent: ModuleButton, setting: BooleanSetting) : Element<BooleanSetting>(
    parent, setting, ElementType.CHECK_BOX
) {

    private val colorAnim = ColorAnimation(150)
    override val isHovered: Boolean get() = isAreaHovered(x + width - 30f, y + 5f, 21f, 20f)

    override fun draw(vg: VG) {
        vg.nanoVG {
            drawRect(x, y, width, height, ColorUtil.elementBackground)
            drawText(displayName, x + 6, y + height / 2, -1, 16f, Fonts.REGULAR)
            drawDropShadow(x + width - 30f, y + 5f, 21f, 20f, 10f, 0.75f, 5f)
            val color = colorAnim.get(ColorUtil.clickGUIColor, Color(38, 38, 38), setting.enabled).rgb
            drawRoundedRect(x + width - 30f, y + 5f, 21f, 20f, 5f, color)
            drawHollowRoundedRect(x + width - 31f, y + 5f, 21.5f, 20f, 4.5f, ColorUtil.clickGUIColor.rgb, 1.5f)
            if (isHovered) drawHollowRoundedRect(x + width - 31f, y + 4f, 21.5f, 21f, 4.5f, ColorUtil.boxHoverColor, 1.5f)
        }
    }

    override fun mouseClicked(mouseButton: Int): Boolean {
        if (mouseButton == 0 && isHovered) {
            if (colorAnim.start()) setting.toggle()
            return true
        }
        return false
    }
}