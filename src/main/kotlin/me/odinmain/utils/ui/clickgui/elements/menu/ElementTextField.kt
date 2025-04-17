package me.odinmain.utils.ui.clickgui.elements.menu

import me.odinmain.features.settings.impl.StringSetting
import me.odinmain.font.OdinFont
import me.odinmain.utils.render.*
import me.odinmain.utils.ui.clickgui.animations.impl.ColorAnimation
import me.odinmain.utils.ui.clickgui.elements.Element
import me.odinmain.utils.ui.clickgui.elements.ElementType
import me.odinmain.utils.ui.clickgui.elements.ModuleButton
import me.odinmain.utils.ui.clickgui.util.ColorUtil
import me.odinmain.utils.ui.clickgui.util.ColorUtil.brighter
import me.odinmain.utils.ui.clickgui.util.ColorUtil.elementBackground
import me.odinmain.utils.ui.clickgui.util.ColorUtil.textColor
import me.odinmain.utils.ui.clickgui.util.HoverHandler
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

    private val colorAnim = ColorAnimation(100)
    private val hover = HoverHandler(0, 150)

    private val buttonColor: Color
        inline get() = ColorUtil.buttonColor.brighter(1 + hover.percent() / 500f)

    override fun draw() {
        roundedRectangle(x, y, w, h, elementBackground)
        if (getTextWidth(display + "00" + name, 12f) <= w) {
            val width = getTextWidth(display, 12f)
            hover.handle(x + w - 15 - width, y + 4, width + 12f, 22f)
            roundedRectangle(x + w - 15 - width, y + 4, width + 12f, 22f, buttonColor, 5f)

            if (listening || colorAnim.isAnimating()) {
                val color = colorAnim.get(ColorUtil.clickGUIColor, buttonColor, listening)
                rectangleOutline(x + w - 16 - width, y + 3, width + 12.5f, 22.5f, color, 4f,1.5f)
            }

            text(display, x + w - 10, y + 16f, textColor, 12f, OdinFont.REGULAR, TextAlign.Right)
            text(name,  x + 6f, y + h / 2, textColor, 12f)
        } else {
            if (isHovered || listening) {
                val width = getTextWidth(display, 12f)
                hover.handle(x + w - 15 - width, y + 4, width + 12f, 22f)
                roundedRectangle(x + w / 2 - width / 2 - 6, y + 4, width + 12f, 22f, buttonColor, 5f)

                if (listening || colorAnim.isAnimating()) {
                    val color = colorAnim.get(ColorUtil.clickGUIColor, buttonColor, listening)
                    rectangleOutline(x + w / 2 - width / 2 - 7, y + 3, width + 12.5f, 22.5f, color, 4f,3f)
                }

                text(display, x + w / 2f, y + h / 2f, textColor, 12f, OdinFont.REGULAR, TextAlign.Middle)
            }
            else text(name, x + w / 2f, y + h / 2f, textColor, 12f, OdinFont.REGULAR, TextAlign.Middle)
        }
    }

    override fun mouseClicked(mouseButton: Int): Boolean {
        if (mouseButton == 0 && isHovered) {
            if (colorAnim.start()) listening = !listening
            return true
        } else if (listening) {
            if (colorAnim.start()) listening = false
        }
        return false
    }

    override fun keyTyped(typedChar: Char, keyCode: Int): Boolean {
        if (listening) {
            when (keyCode) {
                Keyboard.KEY_ESCAPE, Keyboard.KEY_NUMPADENTER, Keyboard.KEY_RETURN -> if (colorAnim.start()) listening = false
                Keyboard.KEY_BACK -> setting.text = setting.text.dropLast(1)
                !in keyBlackList -> setting.text += typedChar.toString()
            }
            return true
        }
        return false
    }

    companion object {
        val keyBlackList = intArrayOf(
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
}