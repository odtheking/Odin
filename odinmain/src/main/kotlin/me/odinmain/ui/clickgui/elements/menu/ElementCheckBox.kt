package me.odinmain.ui.clickgui.elements.menu

import me.odinmain.features.impl.render.ClickGUIModule
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.ui.clickgui.animations.impl.ColorAnimation
import me.odinmain.ui.clickgui.animations.impl.LinearAnimation
import me.odinmain.ui.clickgui.elements.Element
import me.odinmain.ui.clickgui.elements.ElementType
import me.odinmain.ui.clickgui.elements.ModuleButton
import me.odinmain.ui.clickgui.util.ColorUtil.brighter
import me.odinmain.ui.clickgui.util.ColorUtil.buttonColor
import me.odinmain.ui.clickgui.util.ColorUtil.clickGUIColor
import me.odinmain.ui.clickgui.util.ColorUtil.darker
import me.odinmain.ui.clickgui.util.ColorUtil.darkerIf
import me.odinmain.ui.clickgui.util.ColorUtil.elementBackground
import me.odinmain.ui.clickgui.util.ColorUtil.textColor
import me.odinmain.ui.clickgui.util.HoverHandler
import me.odinmain.ui.util.*
import me.odinmain.ui.util.MouseUtils.isAreaHovered
import me.odinmain.utils.render.Color

/**
 * Renders all the modules.
 *
 * Backend made by Aton, with some changes
 * Design mostly made by Stivais
 *
 * @author Stivais, Aton
 * @see [Element]
 */
class ElementCheckBox(parent: ModuleButton, setting: BooleanSetting) : Element<BooleanSetting>(
    parent, setting, ElementType.CHECK_BOX
) {
    private val colorAnim = ColorAnimation(250)
    private val linearAnimation = LinearAnimation<Float>(200)

    private val hover = HoverHandler(0, 150)

    override val isHovered: Boolean get() =
        if (!ClickGUIModule.switchType) isAreaHovered(x + w - 30f, y + 5f, 21f, 20f)
        else isAreaHovered(x + w - 43f, y + 4f, 34f, 20f)

    override fun draw() {

        roundedRectangle(x, y, w, h, elementBackground)
        text(name, x + 6f, y + h / 2f, textColor, 16f, Fonts.REGULAR)

        hover.handle(x + w - 43f, y + 4f, 34f, 20f)
        val color = colorAnim.get(clickGUIColor, buttonColor, setting.enabled).brighter(1 + hover.percent() / 500f)

        if (!ClickGUIModule.switchType) {
            dropShadow(x + w - 30f, y + 5f, 21f, 20f, 10f, 0.75f, 5f)
            roundedRectangle(x + w - 30f, y + 5f, 21f, 20f, color, 5f)
            rectangleOutline(x + w - 30f, y + 5f, 21f, 20f, clickGUIColor, 5f, 3f)
        } else {
            dropShadow(x + w - 43f, y + 4f, 34f, 20f, 10f, 0.75f, 9f)

            roundedRectangle(x + w - 43f, y + 4f, 34f, 20f, buttonColor, 9f)
            if (setting.enabled || linearAnimation.isAnimating()) roundedRectangle(x + w - 43f, y + 4f, linearAnimation.get(34f, 9f, setting.enabled), 20f, color, 9f)

            if (isHovered) rectangleOutline(x + w - 43f, y + 4f, 34f, 20f, color.darker(.85f), 9f, 3f)
            circle(x + w - linearAnimation.get(33f, 17f, !setting.enabled), y + 14f, 6f,
                Color(220, 220, 220).darkerIf(isHovered, 0.9f)
            )
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