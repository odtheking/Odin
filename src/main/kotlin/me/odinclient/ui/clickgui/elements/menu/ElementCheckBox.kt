package me.odinclient.ui.clickgui.elements.menu

import cc.polyfrost.oneconfig.renderer.font.Fonts
import cc.polyfrost.oneconfig.utils.dsl.*
import me.odinclient.ui.clickgui.elements.Element
import me.odinclient.ui.clickgui.elements.ElementType
import me.odinclient.ui.clickgui.elements.ModuleButton
import me.odinclient.ui.clickgui.util.ColorUtil
import me.odinclient.utils.render.gui.MouseUtils.isAreaHovered
import me.odinclient.features.settings.impl.BooleanSetting
import me.odinclient.utils.render.Color
import me.odinclient.utils.render.gui.GuiUtils.drawOutlineRoundedRect
import me.odinclient.utils.render.gui.GuiUtils.nanoVG
import me.odinclient.utils.render.gui.animations.impl.ColorAnimation
import me.odinclient.utils.render.gui.animations.impl.LinearAnimation

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
            if (!ClickGUIModule.switchType) {
                //val color = linearAnimation.getColor(ColorUtil.clickGUIColor, Color(38, 38, 38), setting.enabled).rgba
                drawRoundedRect(x + width - 30f, y + 5f, 21f, 20f, 5f, -1) // fuck this ill fix it later
                drawOutlineRoundedRect(x + width - 30f, y + 5f, 21f, 20f, 5f, ColorUtil.clickGUIColor.rgba, 1.5f)
                if (isHovered) drawOutlineRoundedRect(
                    x + width - 30f,
                    y + 5f,
                    21f,
                    20f,
                    5f,
                    ColorUtil.boxHoverColor,
                    1.5f
                )
            } else {
                drawRoundedRect(x + width - 55f, y + 2f, 30f, 20f, 5f, if (setting.enabled) ClickGUIModule.color.rgba else Color(38, 38, 38).rgba) // ill fix color stuff later
                drawCircle(x + width - linearAnimation.get(50f, 30f, !setting.enabled), y + 12f, 7f, -1)
            }
        }
    }

    override fun mouseClicked(mouseButton: Int): Boolean {
        if (mouseButton == 0 && isHovered) {
            if (linearAnimation.start()) {
                setting.toggle()
            }
            return true
        }
        return false
    }
}