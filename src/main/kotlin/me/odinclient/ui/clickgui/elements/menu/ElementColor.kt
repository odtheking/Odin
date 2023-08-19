package me.odinclient.ui.clickgui.elements.menu

import cc.polyfrost.oneconfig.renderer.font.Fonts
import me.odinclient.features.settings.impl.ColorSetting
import me.odinclient.ui.clickgui.elements.Element
import me.odinclient.ui.clickgui.elements.ElementType
import me.odinclient.ui.clickgui.elements.ModuleButton
import me.odinclient.ui.clickgui.util.ColorUtil.brighter
import me.odinclient.ui.clickgui.util.ColorUtil.buttonColor
import me.odinclient.ui.clickgui.util.ColorUtil.darker
import me.odinclient.ui.clickgui.util.ColorUtil.elementBackground
import me.odinclient.ui.clickgui.util.ColorUtil.hsbMax
import me.odinclient.ui.clickgui.util.ColorUtil.textColor
import me.odinclient.ui.clickgui.util.ColorUtil.withAlpha
import me.odinclient.ui.clickgui.util.HoverHandler
import me.odinclient.utils.render.Color
import me.odinclient.utils.render.gui.MouseUtils.isAreaHovered
import me.odinclient.utils.render.gui.MouseUtils.mouseX
import me.odinclient.utils.render.gui.MouseUtils.mouseY
import me.odinclient.utils.render.gui.animations.impl.EaseInOut
import me.odinclient.utils.render.gui.nvg.*
import kotlin.math.floor

/**
 * Renders all the modules.
 *
 * Backend made by Aton, with some changes
 * Design mostly made by Stivais
 *
 * @author Stivais, Aton
 * @see [Element]
 */
class ElementColor(parent: ModuleButton, setting: ColorSetting) :
    Element<ColorSetting>(parent, setting, ElementType.COLOR) {

    private val anim = EaseInOut(200)
    var dragging: Int? = null

    inline val color: Color
        get() = setting.value

    private val hover = HoverHandler(0, 150)

    // TODO: MAKE A BETTER DESIGN (FUNCTION IS ALL HERE P MUCH)
    override fun draw(nvg: NVG) {
        h = floor(anim.get(36f, if (setting.allowAlpha) 253f else 233f, !extended))

        hover.handle(x + w - 41, y + 5, 31.5f, 19f)

        nvg {
            rect(x, y, w, h, elementBackground)
            text(name, x + 6f, y + 18f, textColor, 16f, Fonts.REGULAR)
            dropShadow(x + w - 40f, y + 5f, 31f, 19f, 10f, 0.75f, 5f)
            rect(x + w - 40f, y + 5f, 31f, 19f, color.brighter(1 + hover.percent() / 500f), 5f)
            rectOutline(x + w - 40f, y + 5f, 31f, 19f, color.darker().withAlpha(1f), 5f, 1.5f)

            if (!extended && !anim.isAnimating()) return@nvg

            val scissor = scissor(x, y, w, h + 1)

            // SATURATION AND BRIGHTNESS
            drawHSBBox(x + 10f, y + 38f, w - 20f, 170f, color.hsbMax())
            dropShadow(x + 10f, y + 38f, w - 20f, 170f, 10f, 0.5f, 8f)

            val sbPointer = Pair((x + 10f + setting.saturation * 220), (y + 38f + (1 - setting.brightness) * 170))
            dropShadow(sbPointer.first - 8.5f, sbPointer.second - 8.5f, 17f, 17f, 2.5f, 2.5f, 9f)
            circle(sbPointer.first, sbPointer.second, 9f, color.darker(0.5f))
            circle(sbPointer.first, sbPointer.second, 7f, color)

            // HUE

            dropShadow(x + 10f, y + 214f, w - 20f, 15f, 10f, 0.5f, 5f)
            image("/assets/odinclient/HueGradient.png", x + 10f, y + 214f, w - 20f, 15f, 5f, javaClass)
            rectOutline(x + 10f, y + 214f, w - 20f, 15f, buttonColor, 5f, 1f)

            val hue = Pair((x + 10f + setting.hue * 221f), y + 221f)
            dropShadow(hue.first - 8.5f, hue.second - 8.5f, 17f, 17f, 2.5f, 2.5f, 9f)
            circle(hue.first, hue.second, 9f, color.hsbMax().darker(0.5f))
            circle(hue.first, hue.second, 7f, color.hsbMax())

            // ALPHA
            if (setting.allowAlpha) {
                dropShadow(x + 10f, y + 235f, w - 20f, 15f, 10f, 0.5f, 5f)
                gradientRect(x + 10f, y + 235f, w - 20f, 15f, Color.TRANSPARENT, color.withAlpha(1f), 5f)

                val alpha = Pair((x + 10f + setting.alpha * 220f), y + 243f)
                dropShadow(alpha.first - 8.5f, alpha.second - 8.5f, 17f, 17f, 2.5f, 2.5f, 9f)
                circle(alpha.first, alpha.second, 9f, Color(-1, setting.alpha).darker(.5f))
                circle(alpha.first, alpha.second, 7f, Color(-1, setting.alpha))
            }

            when (dragging) {
                0 -> {
                    setting.saturation = (mouseX - (x + 10f)) / 220f
                    setting.brightness = -((mouseY - (y + 38f)) - 170f) / 170f
                }
                1 -> setting.hue = (mouseX - (x + 10f)) / (w - 20f)
                2 -> setting.alpha = (mouseX - (x + 10f)) / (w - 20f)
            }

            resetScissor(scissor)
        }
    }

    override fun mouseClicked(mouseButton: Int): Boolean {
        if (mouseButton == 0) {
            if (isHovered) {
                if (anim.start()) extended = !extended
                return true
            }
            if (!extended) return false

            dragging = when {
                isAreaHovered(x + 10f, y + 38f, w - 20f, 170f) -> 0 // sat & brightness
                isAreaHovered(x + 10f, y + 214f, w - 20f, 15f) -> 1 // hue
                isAreaHovered(x + 10f, y + 235f, w - 20f, 15f) && setting.allowAlpha -> 2 // alpha
                else -> null
            }

        } else if (mouseButton == 1) {
            if (isHovered) {
                if (anim.start()) extended = !extended
                return true
            }
        }
        return false
    }

    override fun mouseReleased(state: Int) {
        dragging = null
    }

    override val isHovered: Boolean
        get() = isAreaHovered(x + w - 41, y + 5, 31.5f, 19f)
}