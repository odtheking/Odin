package me.odinclient.ui.clickgui.elements.menu

import cc.polyfrost.oneconfig.renderer.font.Fonts
import me.odinclient.features.impl.general.ClickGUIModule
import me.odinclient.features.settings.impl.BooleanSetting
import me.odinclient.ui.clickgui.elements.Element
import me.odinclient.ui.clickgui.elements.ElementType
import me.odinclient.ui.clickgui.elements.ModuleButton
import me.odinclient.ui.clickgui.util.ColorUtil.brighter
import me.odinclient.ui.clickgui.util.ColorUtil.buttonColor
import me.odinclient.ui.clickgui.util.ColorUtil.clickGUIColor
import me.odinclient.ui.clickgui.util.ColorUtil.darker
import me.odinclient.ui.clickgui.util.ColorUtil.darkerIf
import me.odinclient.ui.clickgui.util.ColorUtil.elementBackground
import me.odinclient.ui.clickgui.util.ColorUtil.textColor
import me.odinclient.ui.clickgui.util.HoverHandler
import me.odinclient.utils.render.Color
import me.odinclient.utils.render.gui.MouseUtils.isAreaHovered
import me.odinclient.utils.render.gui.animations.impl.ColorAnimation
import me.odinclient.utils.render.gui.animations.impl.LinearAnimation
import me.odinclient.utils.render.gui.nvg.*

class ElementCheckBox(parent: ModuleButton, setting: BooleanSetting) : Element<BooleanSetting>(
    parent, setting, ElementType.CHECK_BOX
) {
    private val colorAnim = ColorAnimation(250)
    private val linearAnimation = LinearAnimation<Float>(200)

    private val hover = HoverHandler(150)

    override val isHovered: Boolean get() =
        if (!ClickGUIModule.switchType) isAreaHovered(x + w - 30f, y + 5f, 21f, 20f)
        else isAreaHovered(x + w - 43f, y + 4f, 34f, 20f)

    override fun draw(nvg: NVG) {
        nvg {
            rect(x, y, w, h, elementBackground)
            text(name, x + 6f, y + h / 2f, textColor, 16f, Fonts.REGULAR)

            hover.handle(x + w - 43f, y + 4f, 34f, 20f)
            val color = colorAnim.get(clickGUIColor, buttonColor, setting.enabled).brighter(1 + hover.percent() / 500f)

            if (!ClickGUIModule.switchType) {
                dropShadow(x + w - 30f, y + 5f, 21f, 20f, 10f, 0.75f, 5f)
                rect(x + w - 30f, y + 5f, 21f, 20f, color, 5f)
                rectOutline(x + w - 30f, y + 5f, 21f, 20f, clickGUIColor, 5f, 1.5f)
            } else {
                dropShadow(x + w - 43f, y + 4f, 34f, 20f, 10f, 0.75f, 5f)
                rect(x + w - 43f, y + 4f, 34f, 20f, color, 9f)
                if (isHovered) rectOutline(x + w - 43f, y + 4f, 34f, 20f, clickGUIColor.darker(), 9f, .5f)
                circle(x + w - linearAnimation.get(35f, 15f, !setting.enabled), y + 14f, 7f,
                    Color(220, 220, 220).darkerIf(isHovered, 0.9f)
                )
            }
        }


        /*
        nvg.nanoVG {
            drawRect(x, y, w, h, 1)
            drawText(name, x + 6, y + h / 2, -1, 16f, Fonts.REGULAR)
            val color = colorAnim.get(ColorUtil.clickGUIColor, Color(38, 38, 38), setting.enabled).rgba
            if (!ClickGUIModule.switchType) {
                drawDropShadow(x + w - 30f, y + 5f, 21f, 20f, 10f, 0.75f, 5f)
                drawRoundedRect(x + w - 30f, y + 5f, 21f, 20f, 5f, color)
                drawOutlineRoundedRect(x + w - 30f, y + 5f, 21f, 20f, 5f, ColorUtil.clickGUIColor.rgba, 1.5f)
                if (isHovered) drawOutlineRoundedRect(
                    x + w - 30f,
                    y + 5f,
                    21f,
                    20f,
                    5f,
                    ColorUtil.boxHoverColor,
                    1.5f
                )
            } else {

            }
        }

         */
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