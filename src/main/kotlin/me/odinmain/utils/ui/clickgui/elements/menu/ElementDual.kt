package me.odinmain.utils.ui.clickgui.elements.menu

import me.odinmain.features.settings.impl.DualSetting
import me.odinmain.font.OdinFont
import me.odinmain.utils.render.TextAlign
import me.odinmain.utils.render.dropShadow
import me.odinmain.utils.render.roundedRectangle
import me.odinmain.utils.render.text
import me.odinmain.utils.ui.Colors
import me.odinmain.utils.ui.clickgui.animations.impl.EaseInOut
import me.odinmain.utils.ui.clickgui.elements.Element
import me.odinmain.utils.ui.clickgui.elements.ElementType
import me.odinmain.utils.ui.clickgui.elements.ModuleButton
import me.odinmain.utils.ui.clickgui.util.ColorUtil.buttonColor
import me.odinmain.utils.ui.clickgui.util.ColorUtil.clickGUIColor
import me.odinmain.utils.ui.clickgui.util.ColorUtil.darkerIf
import me.odinmain.utils.ui.clickgui.util.ColorUtil.elementBackground
import me.odinmain.utils.ui.util.MouseUtils

/**
 * Renders all the modules.
 *
 * Backend made by Aton, with some changes
 * Design mostly made by Stivais
 *
 * @author Stivais, Aton
 * @see [Element]
 */
class ElementDual(parent: ModuleButton, setting: DualSetting) : Element<DualSetting>(
    parent, setting, ElementType.DUAL
) {
    private val posAnim = EaseInOut(250)

    private val isRightHovered: Boolean
        get() = MouseUtils.isAreaHovered(x + w / 2 + 5f, y + 2f, w / 2 - 10f, 30f)

    private val isLeftHovered: Boolean
        get() = MouseUtils.isAreaHovered(x + 5f, y + 2f, w / 2 - 10f, 30f)

    override fun draw() {
        roundedRectangle(x, y, w, h, elementBackground)
        dropShadow(x + 7f, y + 3f, w - 14f, 28f, 10f, 3.75f)
        roundedRectangle(x + 7f, y + 3f, w - 14f, 28f, buttonColor, 5f)

        val pos = posAnim.get(8f, w / 2, !setting.enabled)
        roundedRectangle(x + pos, y + 3f, w / 2 - 6f, 28f, clickGUIColor, 5f)

        text(setting.left, x + w / 4 + 6f, y + 1f + h / 2, Colors.WHITE.darkerIf(isLeftHovered), 12f, OdinFont.REGULAR, TextAlign.Middle)
        text(setting.right, x + w * 3 / 4 - 3f,y + 1f + h / 2, Colors.WHITE.darkerIf(isRightHovered), 12f, OdinFont.REGULAR, TextAlign.Middle)
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