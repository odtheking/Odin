package me.odinclient.ui.clickgui.elements.menu

import cc.polyfrost.oneconfig.renderer.font.Fonts
import cc.polyfrost.oneconfig.utils.dsl.*
import me.odinclient.ui.clickgui.elements.Element
import me.odinclient.ui.clickgui.elements.ElementType
import me.odinclient.ui.clickgui.elements.ModuleButton
import me.odinclient.ui.clickgui.util.ColorUtil
import me.odinclient.features.Module
import me.odinclient.features.settings.impl.DummySetting
import me.odinclient.utils.gui.animations.ColorAnimation
import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse
import java.awt.Color

class ElementKeyBind(parent: ModuleButton, private val mod: Module) :
    Element<DummySetting>(parent, DummySetting("Keybind"), ElementType.KEY_BIND) {

    private val colorAnim = ColorAnimation(100)

    override fun renderElement(vg: VG) {
        val displayValue = if (mod.keyCode > 0)
            Keyboard.getKeyName(mod.keyCode) ?: "Err"
        else if (mod.keyCode < 0)
            Mouse.getButtonName(mod.keyCode + 100)
        else
            "None"

        nanoVG(vg.instance) {
            drawRect(x, y, width, height, ColorUtil.elementBackground)
            val length = getTextWidth(displayValue, 16f, Fonts.REGULAR)
            drawRoundedRect(x + width - 20 - length, y + 4, length + 12f, 22f, 5f, Color(35, 35, 35).rgb)
            drawDropShadow(x + width - 20 - length, y + 4, length + 12f, 22f, 10f, 0.75f, 5f)
            if (listening || colorAnim.isAnimating()) {
                val color = colorAnim.get(ColorUtil.clickGUIColor, Color(35, 35, 35), listening).rgb
                drawHollowRoundedRect(x + width - 21 - length, y + 3, length + 12.5f, 22.5f, 4f, color, 1.5f)
            }

            drawText(displayName,  x + 6f, y + height / 2, -1, 16f, Fonts.REGULAR)
            drawText(displayValue, x + width - 14 - length, y + 16f, -1, 16f, Fonts.REGULAR)
        }
    }

    override fun mouseClicked(mouseButton: Int): Boolean {
        if (mouseButton == 0 && isHovered) {
            if (colorAnim.start()) listening = !listening
            return true
        } else if (listening) {
            mod.keyCode = -100 + mouseButton
            if (colorAnim.start()) listening = false
        }
        return super.mouseClicked(mouseButton)
    }

    override fun keyTyped(typedChar: Char, keyCode: Int): Boolean {
        if (listening) {
            if (keyCode == Keyboard.KEY_ESCAPE || keyCode == Keyboard.KEY_BACK) {
                mod.keyCode = Keyboard.KEY_NONE
                if (colorAnim.start()) listening = false
            } else if (keyCode == Keyboard.KEY_NUMPADENTER || keyCode == Keyboard.KEY_RETURN) {
                if (colorAnim.start()) listening = false
            } else if (!keyBlacklist.contains(keyCode)) {
                mod.keyCode = keyCode
                if (colorAnim.start()) listening = false
            }
            return true
        }
        return super.keyTyped(typedChar, keyCode)
    }

    private val keyBlacklist = intArrayOf()
}