package me.odinclient.clickgui.elements.menu

import cc.polyfrost.oneconfig.renderer.font.Fonts
import cc.polyfrost.oneconfig.utils.dsl.VG
import me.odinclient.features.settings.impl.ColorSetting
import me.odinclient.clickgui.elements.Element
import me.odinclient.clickgui.elements.ElementType
import me.odinclient.clickgui.elements.ModuleButton
import me.odinclient.clickgui.util.ColorUtil
import me.odinclient.clickgui.util.ColorUtil.darker
import me.odinclient.clickgui.util.ColorUtil.withAlpha
import me.odinclient.clickgui.util.FontUtil.drawCustomText
import me.odinclient.clickgui.util.FontUtil.getStringWidth
import me.odinclient.clickgui.util.MouseUtils.isActualAreaHovered
import me.odinclient.clickgui.util.MouseUtils.isAreaHovered
import me.odinclient.clickgui.util.MouseUtils.scaledMouseX
import me.odinclient.utils.render.HUDRenderUtils.startDraw
import net.minecraft.util.MathHelper
import org.lwjgl.input.Keyboard
import kotlin.math.roundToInt

class ElementColor(parent: ModuleButton, setting: ColorSetting) :
    Element<ColorSetting>(parent, setting, ElementType.COLOR) {
    var dragging: Int? = null

    override fun renderElement(partialTicks: Float, vg: VG) {
        val colorValue = setting.value

        vg.startDraw(x + (width - 20), y + 2.5, 15.5, 9.5) {
            vg.drawCustomText(displayName, x + 3, y + 8)

            drawShadow()
            roundedRect(colorValue.rgb)
            roundRectOutline(colorValue.darker().rgb, 5f, 1.25f)

            if (isDisplayHovered)
                roundRectOutline(ColorUtil.boxHoverColor, 5f, 1.25f)


            if (extended) {
                var currentY = y + DEFAULT_HEIGHT
                val increment = DEFAULT_HEIGHT

                for (currentColor in setting.colors()) {
                    val isColorDragged = dragging == currentColor.ordinal
                    val displayVal = "${(setting.getNumber(currentColor) * 100.0).roundToInt() / 100.0}"

                    val textWidth = getStringWidth(vg, displayVal, 16f, Fonts.REGULAR) * 2f
                    vg.drawCustomText(displayVal, x + (width - textWidth - 3), currentY + 8)

                    roundedRect(x + 3, currentY + 5.75, width - 31, 3, ColorUtil.sliderBackgroundColor, 2.5f, 2.5f)
                    drawShadow(x + 3, currentY + 5.75, width - 31, 3, 10f, 0.75f, 5f)

                    val percentage = setting.getNumber(currentColor) / currentColor.max
                    if (x + 3 < x + percentage * (width - 6)) {
                        val color = currentColor.color.withAlpha(if (isColorDragged) 255 else 200).rgb
                        roundedRect(x + 3, currentY + 5.75, percentage * (width - 31), 3, color, 2.5f, 2.5f)
                    }

                    if (isColorDragged) {
                        val newVal = MathHelper.clamp_double((scaledMouseX - x).toDouble() / (width - textWidth - 9), 0.0, 1.0) * currentColor.max
                        setting.setNumber(currentColor, newVal)
                    }
                    currentY += increment
                }
            }
        }
    }

    override fun mouseClicked(mouseButton: Int): Boolean {
        if (mouseButton == 0) {
            if (isButtonHovered) {
                extended = !extended
                return true
            }

            if (!extended) return false
            var currentY = DEFAULT_HEIGHT
            for (currentColor in setting.colors()) {
                if (isColorHovered(y + currentY)) {
                    dragging = currentColor.ordinal
                    return true
                }
                currentY += DEFAULT_HEIGHT
            }
        } else if (mouseButton == 1) {
            if (isButtonHovered) {
                extended = !extended
                return true
            }
        }
        return super.mouseClicked(mouseButton)
    }

    override fun mouseReleased(state: Int) {
        dragging = null
    }

    override fun keyTyped(typedChar: Char, keyCode: Int): Boolean {
        if (!extended) return false

        var currentY = DEFAULT_HEIGHT

        for (currentColor in setting.colors()) {
            if (isColorHovered(y + currentY)) {
                when (keyCode) {
                    Keyboard.KEY_RIGHT -> {
                        setting.setNumber(currentColor, setting.getNumber(currentColor) + currentColor.max / 255.0)
                        return true
                    }
                    Keyboard.KEY_LEFT -> {
                        setting.setNumber(currentColor, setting.getNumber(currentColor) - currentColor.max / 255.0)
                        return true
                    }
                }
            }
            currentY += DEFAULT_HEIGHT
        }
        return super.keyTyped(typedChar, keyCode)
    }

    private val isButtonHovered
        get() = isAreaHovered(x, y, width, 15)

    private val isDisplayHovered
        get() = isActualAreaHovered(x + (width - 20), y + 2,  x + (width - 1), y + 12)

    private fun isColorHovered(currentY: Int) = // has to be a function for vartiable
        isAreaHovered(x, currentY, width, DEFAULT_HEIGHT)
}