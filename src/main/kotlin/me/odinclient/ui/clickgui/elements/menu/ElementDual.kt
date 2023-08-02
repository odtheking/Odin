package me.odinclient.ui.clickgui.elements.menu

import me.odinclient.features.settings.impl.DualSetting
import me.odinclient.ui.clickgui.elements.Element
import me.odinclient.ui.clickgui.elements.ElementType
import me.odinclient.ui.clickgui.elements.ModuleButton
import me.odinclient.utils.render.gui.MouseUtils
import me.odinclient.utils.render.gui.animations.impl.ColorAnimation
import me.odinclient.utils.render.gui.nvg.NVG

class ElementDual(parent: ModuleButton, setting: DualSetting) : Element<DualSetting>(
    parent, setting, ElementType.DUAL
) {
    private val leftColorAnim = ColorAnimation(150)
    private val rightColorAnim = ColorAnimation(150)
    private val isRightHovered: Boolean get() = MouseUtils.isAreaHovered(x + w / 2 + 5f, y + 2f, w / 2 - 10f, 30f)
    private val isLeftHovered: Boolean get() = MouseUtils.isAreaHovered(x + 5f, y + 2f, w / 2 - 10f, 30f)

    override fun draw(nvg: NVG) {
        /*nvg.nanoVG {
            drawRect(x, y, w, h, 1)
            drawDropShadow(x + 7f, y + 3f, w - 14f, 28f, 10f, 3.75f, 5f)
            drawRoundedRect(x + 7f, y + 3f, w - 14f, 28f, 5f, Color(35, 35, 35).rgba)

            val rightColor = rightColorAnim.get(ColorUtil.clickGUIColor, if (isRightHovered) Color(45, 45, 45) else Color(35, 35, 35), setting.enabled).rgba
            drawRoundedRect(x + w / 2 + 7f, y + 3f, w / 2 - 14f, 28f, 5f, rightColor)

            val leftColor = leftColorAnim.get(ColorUtil.clickGUIColor, if (isLeftHovered) Color(45, 45, 45) else Color(35, 35, 35), !setting.enabled).rgba
            drawRoundedRect(x + 7f, y + 3f, w / 2 - 14f, 28f, 5f, leftColor)

            drawCustomCenteredText(setting.left, x + w / 4, y + 3f + h / 2, 16f, Fonts.REGULAR, -1)
            drawCustomCenteredText(setting.right, x + w * 3 / 4,y + 3f + h / 2,  16f, Fonts.REGULAR, -1)
        }


         */
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