package me.odinclient.clickgui.elements.menu

import cc.polyfrost.oneconfig.utils.dsl.VG
import me.odinclient.features.settings.impl.StringSetting
import me.odinclient.clickgui.elements.Element
import me.odinclient.clickgui.elements.ElementType
import me.odinclient.clickgui.elements.ModuleButton
import org.lwjgl.input.Keyboard

/**
 * Provides a text field element.
 *
 * @author Aton
 */
class ElementTextField(parent: ModuleButton, setting: StringSetting) :
    Element<StringSetting>(parent, setting, ElementType.TEXT_FIELD) {

    override fun renderElement(partialTicks: Float, vg: VG) {
        /*
        val displayValue = setting.text


        /** Rendering the text */
        if (FontUtil.getStringWidth(displayValue + "00" + displayName) <= width) {
            FontUtil.drawString(displayName, 1, 2)
            FontUtil.drawString(displayValue, width - FontUtil.getStringWidth(displayValue), 2)
        }else {
            if (isTextHovered(mouseX, mouseY) || listening) {
                FontUtil.drawCenteredStringWithShadow(displayValue, width / 2.0,  2.0)
            } else {
                FontUtil.drawCenteredString(displayName, width / 2.0, 2.0)
            }
        }

         */
    }

    /**
     * Handles interaction with this element.
     * Returns true if interacted with the element to cancel further interactions.
     */
    override fun mouseClicked(mouseButton: Int): Boolean {
        if (mouseButton == 0) {
            listening = true
            return true
        }
        return super.mouseClicked(mouseButton)
    }

    /**
     * Register key strokes.
     */
    override fun keyTyped(typedChar: Char, keyCode: Int): Boolean {
        if (listening) {
            if (keyCode == Keyboard.KEY_ESCAPE || keyCode == Keyboard.KEY_NUMPADENTER || keyCode == Keyboard.KEY_RETURN) {
                listening = false
            } else if (keyCode == Keyboard.KEY_BACK) {
                setting.text = setting.text.dropLast(1)
            }else if (!keyBlackList.contains(keyCode)) {
                setting.text = setting.text + typedChar.toString()
            }
            return true
        }
        return super.keyTyped(typedChar, keyCode)
    }

    /**
     * Checks whether the mouse is hovering the text field
     */
    /*
    private fun isTextHovered(mouseX: Int, mouseY: Int): Boolean {
        return mouseX >= x && mouseX <= x + width && mouseY >= y  && mouseY <= y + height
    }
    */

    private val keyBlackList = intArrayOf(
        Keyboard.KEY_LSHIFT,
        Keyboard.KEY_RSHIFT,
        Keyboard.KEY_UP,
        Keyboard.KEY_RIGHT,
        Keyboard.KEY_LEFT,
        Keyboard.KEY_DOWN,
        Keyboard.KEY_END,
        Keyboard.KEY_NUMLOCK,
        Keyboard.KEY_DELETE,
        Keyboard.KEY_LCONTROL,
        Keyboard.KEY_RCONTROL,
        Keyboard.KEY_CAPITAL,
        Keyboard.KEY_LMENU,
        Keyboard.KEY_F1,
        Keyboard.KEY_F2,
        Keyboard.KEY_F3,
        Keyboard.KEY_F4,
        Keyboard.KEY_F5,
        Keyboard.KEY_F6,
        Keyboard.KEY_F7,
        Keyboard.KEY_F8,
        Keyboard.KEY_F9,
        Keyboard.KEY_F10,
        Keyboard.KEY_F11,
        Keyboard.KEY_F12,
        Keyboard.KEY_F13,
        Keyboard.KEY_F14,
        Keyboard.KEY_F15,
        Keyboard.KEY_F16,
        Keyboard.KEY_F17,
        Keyboard.KEY_F18,
        Keyboard.KEY_F19,
        Keyboard.KEY_SCROLL,
        Keyboard.KEY_RMENU,
        Keyboard.KEY_LMETA,
        Keyboard.KEY_RMETA,
        Keyboard.KEY_FUNCTION,
        Keyboard.KEY_PRIOR,
        Keyboard.KEY_NEXT,
        Keyboard.KEY_INSERT,
        Keyboard.KEY_HOME,
        Keyboard.KEY_PAUSE,
        Keyboard.KEY_APPS,
        Keyboard.KEY_POWER,
        Keyboard.KEY_SLEEP,
        Keyboard.KEY_SYSRQ,
        Keyboard.KEY_CLEAR,
        Keyboard.KEY_SECTION,
        Keyboard.KEY_UNLABELED,
        Keyboard.KEY_KANA,
        Keyboard.KEY_CONVERT,
        Keyboard.KEY_NOCONVERT,
        Keyboard.KEY_YEN,
        Keyboard.KEY_CIRCUMFLEX,
        Keyboard.KEY_AT,
        Keyboard.KEY_UNDERLINE,
        Keyboard.KEY_KANJI,
        Keyboard.KEY_STOP,
        Keyboard.KEY_AX,
        Keyboard.KEY_TAB,
    )
}