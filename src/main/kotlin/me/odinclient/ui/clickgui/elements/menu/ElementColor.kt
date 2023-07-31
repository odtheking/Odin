package me.odinclient.ui.clickgui.elements.menu

import cc.polyfrost.oneconfig.renderer.font.Fonts
import cc.polyfrost.oneconfig.utils.dsl.*
import me.odinclient.features.settings.impl.ColorSetting
import me.odinclient.ui.clickgui.elements.Element
import me.odinclient.ui.clickgui.elements.ElementType
import me.odinclient.ui.clickgui.elements.ModuleButton
import me.odinclient.ui.clickgui.util.ColorUtil
import me.odinclient.ui.clickgui.util.ColorUtil.darker
import me.odinclient.ui.clickgui.util.ColorUtil.elementBackground
import me.odinclient.ui.clickgui.util.ColorUtil.hsbMax
import me.odinclient.ui.clickgui.util.ColorUtil.withAlpha
import me.odinclient.utils.render.Color
import me.odinclient.utils.render.gui.GuiUtils.drawHSBBox
import me.odinclient.utils.render.gui.GuiUtils.drawOutlineRoundedRect
import me.odinclient.utils.render.gui.GuiUtils.nanoVG
import me.odinclient.utils.render.gui.GuiUtils.resetScissor
import me.odinclient.utils.render.gui.GuiUtils.scissor
import me.odinclient.utils.render.gui.MouseUtils.isAreaHovered
import me.odinclient.utils.render.gui.MouseUtils.mouseX
import me.odinclient.utils.render.gui.MouseUtils.mouseY
import me.odinclient.utils.render.gui.animations.impl.EaseInOut
import kotlin.math.floor

class ElementColor(parent: ModuleButton, setting: ColorSetting) :
    Element<ColorSetting>(parent, setting, ElementType.COLOR) {

    private val anim = EaseInOut(200)
    var dragging: Int? = null

    // TODO: MAKE A BETTER DESIGN (FUNCTION IS ALL HERE P MUCH)
    override fun draw(vg: VG) {
        height = floor(anim.get(36f, if (setting.allowAlpha) 253f else 233f, !extended))
        val colorValue = setting.value

        vg.nanoVG {
            drawRect(x, y, width, height, elementBackground)
            drawText(displayName, x + 6f, y + 18f, -1, 16f, Fonts.REGULAR)
            drawDropShadow(x + width - 40f, y + 5f, 31f, 19f, 10f, 0.75f, 5f)
            drawRoundedRect(x + width - 40f, y + 5f, 31f, 19f, 5f, setting.rgb)
            drawOutlineRoundedRect(x + width - 40f, y + 5f, 31f, 19f, 5f, colorValue.darker().rgba, 1.5f)
            if (isHovered) drawOutlineRoundedRect(x + width - 40f, y + 5f, 31f, 19f, 5f, ColorUtil.boxHoverColor, 1.5f)

            if (!extended && !anim.isAnimating()) return@nanoVG

            val scissor = scissor(x, y, width, height + 1)

            // SATURATION AND BRIGHTNESS
            drawHSBBox(x + 10f, y + 38f, width - 20f, 170f, colorValue.hsbMax().rgba)
            drawDropShadow(x + 10f, y + 38f, width - 20f, 170f, 10f, 0.5f, 8f)

            val sbPointer = Pair((x + 10f + setting.saturation * 220), (y + 38f + (1 - setting.brightness) * 170))
            drawDropShadow(sbPointer.first - 8.5f, sbPointer.second - 8.5f, 17f, 17f, 2.5f, 2.5f, 9f)
            drawCircle(sbPointer.first, sbPointer.second, 9f, colorValue.darker(0.5f).rgba)
            drawCircle(sbPointer.first, sbPointer.second, 7f, colorValue.rgba)

            // HUE
            drawDropShadow(x + 10f, y + 214f, width - 20f, 15f, 10f, 0.5f, 5f)
            drawRoundedImage("/assets/odinclient/HueGradient.png", x + 10f, y + 214f, width - 20f, 15f, 5f, javaClass)
            drawOutlineRoundedRect(x + 10f, y + 214f, width - 20f, 15f, 5f, Color(38, 38, 38).rgba, 1f)

            val hue = Pair((x + 10f + setting.hue * 221f), y + 221f)
            drawDropShadow(hue.first - 8.5f, hue.second - 8.5f, 17f, 17f, 2.5f, 2.5f, 9f)
            drawCircle(hue.first, hue.second, 9f, colorValue.hsbMax().darker(0.5f).rgba)
            drawCircle(hue.first, hue.second, 7f, colorValue.hsbMax().rgba)

            // ALPHA
            if (setting.allowAlpha) {
                drawDropShadow(x + 10f, y + 235f, width - 20f, 15f, 10f, 0.5f, 5f)
                //drawRoundedImage("/assets/odinclient/AlphaGrid.png", x + 10f, y + 235f, width - 20f, 15f, 5f, javaClass)
                drawGradientRoundedRect(x + 10f, y + 235f, width - 20f, 15f, Color.TRANSPARENT.rgba, colorValue.withAlpha(1f).rgba, 5f)

                val alpha = Pair((x + 10f + setting.alpha * 220f), y + 243f)
                drawDropShadow(alpha.first - 8.5f, alpha.second - 8.5f, 17f, 17f, 2.5f, 2.5f, 9f)
                drawCircle(alpha.first, alpha.second, 9f, Color(-1, setting.alpha).darker(.5f).rgba)
                drawCircle(alpha.first, alpha.second, 7f, Color(-1, setting.alpha).rgba)
            }

            when (dragging) {
                0 -> {
                    setting.saturation = (mouseX - (x + 10f)) / 220f
                    setting.brightness = -((mouseY - (y + 38f)) - 170f) / 170f
                }
                1 -> setting.hue = (mouseX - (x + 10f)) / (width - 20f)
                2 -> setting.alpha = (mouseX - (x + 10f)) / (width - 20f)
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
                isAreaHovered(x + 10f, y + 38f, width - 20f, 170f) -> 0 // sat & brightness
                isAreaHovered(x + 10f, y + 214f, width - 20f, 15f) -> 1 // hue
                isAreaHovered(x + 10f, y + 235f, width - 20f, 15f) && setting.allowAlpha -> 2 // alpha
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
        get() = isAreaHovered(x + width - 41, y + 5, 31.5f, 19f)
}