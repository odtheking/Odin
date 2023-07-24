package me.odinclient.ui.clickgui.elements.menu

import cc.polyfrost.oneconfig.renderer.font.Fonts
import cc.polyfrost.oneconfig.utils.dsl.*
import me.odinclient.features.settings.impl.DualSetting
import me.odinclient.ui.clickgui.elements.Element
import me.odinclient.ui.clickgui.elements.ElementType
import me.odinclient.ui.clickgui.elements.ModuleButton
import me.odinclient.ui.clickgui.util.ColorUtil
import me.odinclient.utils.render.gui.GuiUtils.drawCustomCenteredText
import me.odinclient.utils.render.gui.GuiUtils.nanoVG
import me.odinclient.utils.render.gui.MouseUtils
import me.odinclient.utils.render.gui.animations.impl.ColorAnimation
import java.awt.Color

class ElementDual(parent: ModuleButton, setting: DualSetting) : Element<DualSetting>(
    parent, setting, ElementType.DUAL
) {
    private val leftColorAnim = ColorAnimation(150)
    private val rightColorAnim = ColorAnimation(150)
    private val isRightHovered: Boolean get() = MouseUtils.isAreaHovered(x + width / 2 + 5f, y + 2f, width / 2 - 10f, 30f)
    private val isLeftHovered: Boolean get() = MouseUtils.isAreaHovered(x + 5f, y + 2f, width / 2 - 10f, 30f)

    override fun draw(vg: VG) {
        vg.nanoVG {
            drawRect(x, y, width, height, ColorUtil.elementBackground)
            drawDropShadow(x + 7f, y + 3f, width - 14f, 28f, 10f, 3.75f, 5f)
            drawRoundedRect(x + 7f, y + 3f, width - 14f, 28f, 5f, Color(35, 35, 35).rgb)

            val rightColor = rightColorAnim.get(ColorUtil.clickGUIColor, if (isRightHovered) Color(45, 45, 45) else Color(35, 35, 35), setting.enabled).rgb
            drawRoundedRect(x + width / 2 + 7f, y + 3f, width / 2 - 14f, 28f, 5f, rightColor)

            val leftColor = leftColorAnim.get(ColorUtil.clickGUIColor, if (isLeftHovered) Color(45, 45, 45) else Color(35, 35, 35), !setting.enabled).rgb
            drawRoundedRect(x + 7f, y + 3f, width / 2 - 14f, 28f, 5f, leftColor)

            drawCustomCenteredText(setting.firstOption, x + width / 4, y + 3f + height / 2, 16f, Fonts.REGULAR, -1)
            drawCustomCenteredText(setting.secondOption, x + width * 3 / 4,y + 3f + height / 2,  16f, Fonts.REGULAR, -1)
        }

    }

    override fun mouseClicked(mouseButton: Int): Boolean {
        if (mouseButton != 0) return false
        if (isLeftHovered && setting.enabled) {
            if (leftColorAnim.start()) setting.enabled = false
            return true
        } else if (isRightHovered && !setting.enabled) {
            if (rightColorAnim.start()) setting.enabled = true
            return true
        }
        return false
    }
}