package me.odinclient.clickgui.elements.menu

import cc.polyfrost.oneconfig.renderer.font.Fonts
import cc.polyfrost.oneconfig.utils.dsl.VG
import me.odinclient.clickgui.elements.Element
import me.odinclient.clickgui.elements.ElementType
import me.odinclient.clickgui.elements.ModuleButton
import me.odinclient.clickgui.util.ColorUtil
import me.odinclient.clickgui.util.FontUtil.drawCustomText
import me.odinclient.clickgui.util.FontUtil.getStringWidth
import me.odinclient.clickgui.util.MouseUtils.isAreaHovered
import me.odinclient.features.Module
import me.odinclient.features.settings.impl.DummySetting
import me.odinclient.utils.render.HUDRenderUtils.startDraw
import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse

class ElementKeyBind(parent: ModuleButton, private val mod: Module) :
    Element<DummySetting>(parent, DummySetting("Keybind"), ElementType.KEY_BIND) {

    private val isKeybindHovered
        get() = isAreaHovered(x, y, width, height)

    override fun renderElement(partialTicks: Float, vg: VG) {
        val displayValue = if (mod.keyCode > 0)
            Keyboard.getKeyName(mod.keyCode) ?: "Err"
        else if (mod.keyCode < 0)
            Mouse.getButtonName(mod.keyCode + 100)
        else
            "None"

        val length = getStringWidth(vg, displayValue, 16f, Fonts.REGULAR) * 2f

        vg.startDraw(x + (width - 10 - length), y + 2, length + 6, 11) {
            roundedRect(color = ColorUtil.clickableColor, top = 5f, bottom = 5f)
            drawShadow(blur = 10f, spread = 0.75f, radius = 5f)

            if (listening)
               roundRectOutline(color = ColorUtil.clickGUIColor.rgb, radius = 5f, thickness = 1.25f)

            vg.drawCustomText(displayName,  x + 3, y + 8.5)
            vg.drawCustomText(displayValue, x + (width - 7 - length), y + 8)
        }
    }

    override fun mouseClicked(mouseButton: Int): Boolean {
        if (mouseButton == 0 && isKeybindHovered) {
            listening = !listening
            return true
        } else if (listening) {
            mod.keyCode = -100 + mouseButton
            listening = false
        }
        return super.mouseClicked(mouseButton)
    }

    override fun keyTyped(typedChar: Char, keyCode: Int): Boolean {
        if (listening) {
            if (keyCode == Keyboard.KEY_ESCAPE || keyCode == Keyboard.KEY_BACK) {
                mod.keyCode = Keyboard.KEY_NONE
                listening = false
            } else if (keyCode == Keyboard.KEY_NUMPADENTER || keyCode == Keyboard.KEY_RETURN) {
                listening = false
            } else {
                mod.keyCode = keyCode
                listening = false
            }
            return true
        }
        return super.keyTyped(typedChar, keyCode)
    }
}