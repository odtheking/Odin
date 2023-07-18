package me.odinclient.ui.clickgui.elements.menu

import cc.polyfrost.oneconfig.renderer.font.Fonts
import cc.polyfrost.oneconfig.utils.dsl.*
import me.odinclient.ui.clickgui.elements.Element
import me.odinclient.ui.clickgui.elements.ElementType
import me.odinclient.ui.clickgui.elements.ModuleButton
import me.odinclient.ui.clickgui.util.ColorUtil
import me.odinclient.ui.clickgui.util.ColorUtil.elementBackground
import me.odinclient.ui.clickgui.util.ColorUtil.withAlpha
import me.odinclient.ui.clickgui.util.MouseUtils.isAreaHovered
import me.odinclient.ui.clickgui.util.MouseUtils.mouseX
import me.odinclient.features.settings.impl.ColorSetting
import me.odinclient.utils.gui.GuiUtils.resetScissor
import me.odinclient.utils.gui.GuiUtils.scissor
import me.odinclient.utils.gui.animations.EaseInOut
import net.minecraft.util.MathHelper
import org.lwjgl.input.Keyboard
import kotlin.math.floor
import kotlin.math.roundToInt

class ElementColor(parent: ModuleButton, setting: ColorSetting) :
    Element<ColorSetting>(parent, setting, ElementType.COLOR) {

    private val openAnim = EaseInOut(200)
    var dragging: Int? = null

    override fun renderElement(vg: VG) {
        val colorValue = setting.value

        nanoVG(vg.instance) {
            height = floor(openAnim.get(36f, DEFAULT_HEIGHT * (if (setting.allowAlpha) 5 else 4), !extended))
            drawRect(x, y, width, height, elementBackground)
            drawText(displayName, x + 6, y + 18f, -1, 16f, Fonts.REGULAR)
            drawDropShadow(x + width - 40, y + 5, 31, 19, 10f, 0.75f, 5f)
            drawRoundedRect(x + width - 40, y + 5, 31, 19, 5f, colorValue.rgb)
            drawHollowRoundedRect(x + width - 41, y + 4, 31.5f, 19.5f, 4f, colorValue.darker().rgb, 1.5f)
            if (isDisplayHovered) drawHollowRoundedRect(x + width - 41f, y + 4f, 31.5f, 19.5f, 4f, ColorUtil.boxHoverColor, 1.5f)

            if (extended || openAnim.isAnimating()) {
                val scissor = scissor(x, y, width, height + 1)
                var currentY = y + DEFAULT_HEIGHT

                for (currentColor in setting.colors) {
                    val isColorDragged = dragging == currentColor.ordinal
                    val displayVal = "${(setting.getNumber(currentColor).roundToInt())}"
                    val textWidth = getTextWidth(displayVal, 16f, Fonts.REGULAR)
                    vg.drawText(displayVal, x + width - textWidth - 6, currentY + 16, -1, 16f,Fonts.REGULAR)

                    drawRoundedRect(x + 6, currentY + 11.5, width - 62, 6, 2.5f, ColorUtil.sliderBackgroundColor)
                    drawDropShadow(x + 6, currentY + 11.5, width - 62, 6, 10f, 0.75f, 5f)

                    val percentage = setting.getNumber(currentColor) / 255
                    if (x + 6 < x + percentage * (width - 12)) {
                        val color = currentColor.color.withAlpha(if (isColorDragged) 255 else 200).rgb
                        drawRoundedRect(x + 6, currentY + 11.5, percentage * (width - 62), 6, 2.5f, color)
                    }

                    if (isColorDragged) {
                        val newVal = MathHelper.clamp_float((mouseX - x) / (width - textWidth - 18), 0.0f, 1.0f) * 255.0
                        setting.setNumber(currentColor, newVal)
                    }
                    currentY += DEFAULT_HEIGHT
                }
                resetScissor(scissor)
            }
        }
    }

    override fun mouseClicked(mouseButton: Int): Boolean {
        if (mouseButton == 0) {
            if (isDisplayHovered) {
                if (openAnim.start()) extended = !extended
                return true
            }
            if (!extended) return false
            var currentY = DEFAULT_HEIGHT
            for (currentColor in setting.colors) {
                if (isColorHovered(y + currentY)) {
                    dragging = currentColor.ordinal
                    return true
                }
                currentY += DEFAULT_HEIGHT
            }
        } else if (mouseButton == 1) {
            if (isDisplayHovered) {
                if (openAnim.start()) extended = !extended
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
        for (currentColor in setting.colors) {
            if (isColorHovered(y + currentY)) {
                val amount = when (keyCode) {
                    Keyboard.KEY_RIGHT -> 255f
                    Keyboard.KEY_LEFT -> -255f
                    else -> return super.keyTyped(typedChar, keyCode)
                }
                setting.setNumber(currentColor, setting.getNumber(currentColor) + amount / 255.0)
            }
            currentY += DEFAULT_HEIGHT
        }
        return super.keyTyped(typedChar, keyCode)
    }

    private val isDisplayHovered get() = isAreaHovered(x + width - 41, y + 5, 31.5f, 19f)
    private fun isColorHovered(currentY: Float) = isAreaHovered(x, currentY, width, DEFAULT_HEIGHT)
}