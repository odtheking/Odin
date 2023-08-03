package me.odinclient.ui.clickgui.elements.menu

import cc.polyfrost.oneconfig.renderer.font.Fonts
import me.odinclient.features.settings.impl.DualSetting
import me.odinclient.ui.clickgui.elements.Element
import me.odinclient.ui.clickgui.elements.ElementType
import me.odinclient.ui.clickgui.elements.ModuleButton
import me.odinclient.ui.clickgui.util.ColorUtil
import me.odinclient.ui.clickgui.util.ColorUtil.darker
import me.odinclient.ui.clickgui.util.ColorUtil.darkerIf
import me.odinclient.utils.render.Color
import me.odinclient.utils.render.gui.MouseUtils
import me.odinclient.utils.render.gui.animations.impl.ColorAnimation
import me.odinclient.utils.render.gui.animations.impl.LinearAnimation
import me.odinclient.utils.render.gui.nvg.*

class ElementDual(parent: ModuleButton, setting: DualSetting) : Element<DualSetting>(
    parent, setting, ElementType.DUAL
) {
    private val posAnim = LinearAnimation<Float>(250)
    private val isRightHovered: Boolean get() = MouseUtils.isAreaHovered(x + w / 2 + 5f, y + 2f, w / 2 - 10f, 30f)
    private val isLeftHovered: Boolean get() = MouseUtils.isAreaHovered(x + 5f, y + 2f, w / 2 - 10f, 30f)

    override fun draw(nvg: NVG) {
        nvg {
            rect(x, y, w, h, ColorUtil.elementBackground)
            dropShadow(x + 7f, y + 3f, w - 14f, 28f, 10f, 3.75f, 5f)
            rect(x + 7f, y + 3f, w - 14f, 28f, Color(35, 35, 35), 5f)

            val pos = posAnim.get(7f, w / 2 + 6f, !setting.enabled)
            rect(x + pos, y + 3f, w / 2 - 6f, 28f, ColorUtil.clickGUIColor, 5f)

            text(setting.left, x + w / 4 + 4f, y + 3f + h / 2, Color.WHITE.darkerIf(isLeftHovered), 16f, Fonts.REGULAR, TextAlign.Middle)
            text(setting.right, x + w * 3 / 4 + 4f,y + 3f + h / 2,  Color.WHITE.darkerIf(isRightHovered), 16f, Fonts.REGULAR, TextAlign.Middle)
        }
    }

    override fun mouseClicked(mouseButton: Int): Boolean {
        if (mouseButton != 0) return false
        if (isLeftHovered && setting.enabled) {
            if (posAnim.start()) setting.enabled = false
            return true
        } else if (isRightHovered && !setting.enabled) {
            if (posAnim.start()) setting.enabled = true
            return true
        }
        return false
    }
}