package me.odinclient.ui.clickgui.elements.menu

import cc.polyfrost.oneconfig.renderer.font.Fonts.REGULAR
import me.odinclient.features.settings.impl.StringSetting
import me.odinclient.ui.clickgui.elements.Element
import me.odinclient.ui.clickgui.elements.ElementType
import me.odinclient.ui.clickgui.elements.ModuleButton
import me.odinclient.ui.clickgui.util.ColorUtil.elementBackground
import me.odinclient.ui.clickgui.util.ColorUtil.textColor
import me.odinclient.utils.render.gui.nvg.*
import org.lwjgl.input.Keyboard

/**
 * Renders all the modules.
 *
 * Backend made by Aton, with some changes
 * Design mostly made by Stivais
 *
 * @author Stivais, Aton
 * @see [Element]
 */
class ElementTextField(parent: ModuleButton, setting: StringSetting) :
    Element<StringSetting>(parent, setting, ElementType.TEXT_FIELD) {

    val display: String
        inline get() = setting.text

    // TODO: MAKE LOOK GOOD!!!!
    override fun draw(nvg: NVG) {
        nvg {
            rect(x, y, w, h, elementBackground)

            if (getTextWidth(display + "00" + name, 16f, REGULAR) <= w) {
                text(name, x + 4, y + h / 2, textColor, 16f, REGULAR)
                text(display, x + (w - getTextWidth(display, 16f, REGULAR) - 4f), y + h / 2, textColor, 16f, REGULAR)
            } else {
                if (isHovered || listening) text(display, x + w / 2f, y + h / 2f, textColor, 16f, REGULAR, TextAlign.Middle)
                else text(name, x + w / 2f, y + h / 2f, textColor, 16f, REGULAR, TextAlign.Middle)
            }
        }
    }

    override fun mouseClicked(mouseButton: Int): Boolean {
        if (mouseButton == 0 && isHovered) {
            listening = true
            return true
        }
        return false
    }

    override fun keyTyped(typedChar: Char, keyCode: Int): Boolean {
        if (listening) {
            when (keyCode) {
                Keyboard.KEY_ESCAPE, Keyboard.KEY_NUMPADENTER, Keyboard.KEY_RETURN -> listening = false
                Keyboard.KEY_BACK -> setting.text = setting.text.dropLast(1)
                !in keyBlackList ->  setting.text = setting.text + typedChar.toString()
            }
            return true
        }
        return false
    }

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

    operator fun Int.contains(intArray: IntArray): Boolean {
        return intArray.contains(this)
    }
}