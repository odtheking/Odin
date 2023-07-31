package me.odinclient.ui.clickgui.elements.menu

import cc.polyfrost.oneconfig.renderer.font.Fonts
import cc.polyfrost.oneconfig.utils.dsl.*
import me.odinclient.features.impl.general.ClickGUIModule
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
    private val colorAnim = ColorAnimation(250)
    private val linearAnimation = LinearAnimation<Float>(200)

    override val isHovered: Boolean get() =
        if (!ClickGUIModule.switchType)
            isAreaHovered(x + width - 30f, y + 5f, 21f, 20f)
        else
            isAreaHovered(x + width - 43f, y + 4f, 34f, 20f)

    override fun draw(vg: VG) {
        vg.nanoVG {
            drawRect(x, y, width, height, ColorUtil.elementBackground)
            drawText(displayName, x + 6, y + height / 2, -1, 16f, Fonts.REGULAR)
            val color = colorAnim.get(ColorUtil.clickGUIColor, Color(38, 38, 38), setting.enabled).rgba
            if (!ClickGUIModule.switchType) {
                drawDropShadow(x + width - 30f, y + 5f, 21f, 20f, 10f, 0.75f, 5f)
                drawRoundedRect(x + width - 30f, y + 5f, 21f, 20f, 5f, color)
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
                drawRoundedRect(x + width - 43f, y + 4f, 34f, 20f, 9f, color)
                if (isHovered) drawOutlineRoundedRect(x + width - 43f, y + 4f, 34f, 20f, 9f, ColorUtil.boxHoverColor, 1.5f)
                drawCircle(x + width - linearAnimation.get(35f, 15f, !setting.enabled), y + 14f, 7f, if (isHovered) Color(220, 220, 220).rgba else -1)
            }
        }
    }

    override fun mouseClicked(mouseButton: Int): Boolean {
        if (mouseButton == 0 && isHovered) {
            if (colorAnim.start()) {
                linearAnimation.start()
                setting.toggle()
            }
            return true
        }
        return false
    }
}