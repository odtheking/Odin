package me.odinmain.ui.clickgui.elements.menu

import cc.polyfrost.oneconfig.renderer.font.Fonts
import me.odin.utils.render.Color
import me.odin.utils.render.gui.animations.impl.ColorAnimation
import me.odin.utils.render.gui.nvg.*
import me.odinmain.features.settings.impl.DummySetting
import me.odinmain.ui.clickgui.elements.Element
import me.odinmain.ui.clickgui.elements.ElementType
import me.odinmain.ui.clickgui.elements.ModuleButton
import me.odinmain.ui.clickgui.util.ColorUtil
import me.odinmain.ui.clickgui.util.ColorUtil.brighter
import me.odinmain.ui.clickgui.util.ColorUtil.clickGUIColor
import me.odinmain.ui.clickgui.util.ColorUtil.elementBackground
import me.odinmain.ui.clickgui.util.ColorUtil.textColor
import me.odinmain.ui.clickgui.util.HoverHandler
import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse

/**
 * Renders all the modules.
 *
 * Backend made by Aton, with some changes
 * Design mostly made by Stivais
 *
 * @author Stivais, Aton
 * @see [Element]
 */
class ElementKeyBind(parent: ModuleButton, private val mod: Module) :
    Element<DummySetting>(parent, DummySetting("Keybind"), ElementType.KEY_BIND) {

    private val colorAnim = ColorAnimation(100)

    private val hover = HoverHandler(0, 150)

    private val buttonColor: Color
        inline get() = ColorUtil.buttonColor.brighter(1 + hover.percent() / 500f)

    override fun draw(nvg: NVG) {
        val value = if (mod.keyCode > 0) Keyboard.getKeyName(mod.keyCode) ?: "Err"
        else if (mod.keyCode < 0) Mouse.getButtonName(mod.keyCode + 100)
        else "None"

        nvg {
            rect(x, y, w, h, elementBackground)

            val width = getTextWidth(value, 16f, Fonts.REGULAR)
            hover.handle(x + w - 20 - width, y + 4, width + 12f, 22f)

            rect(x + w - 20 - width, y + 4, width + 12f, 22f, buttonColor, 5f)
            dropShadow(x + w - 20 - width, y + 4, width + 12f, 22f, 10f, 0.75f, 5f)

            if (listening || colorAnim.isAnimating()) {
                val color = colorAnim.get(clickGUIColor, buttonColor, listening)
                rectOutline(x + w - 21 - width, y + 3, width + 12.5f, 22.5f, color, 4f,1.5f)
            }

            text(name,  x + 6f, y + h / 2, textColor, 16f, Fonts.REGULAR)
            text(value, x + w - 14, y + 16f, textColor, 16f, Fonts.REGULAR, TextAlign.Right)
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
        return false
    }

    override fun keyTyped(typedChar: Char, keyCode: Int): Boolean {
        if (listening) {
            if (keyCode == Keyboard.KEY_ESCAPE || keyCode == Keyboard.KEY_BACK) {
                mod.keyCode = Keyboard.KEY_NONE
                if (colorAnim.start()) listening = false
            } else if (keyCode == Keyboard.KEY_NUMPADENTER || keyCode == Keyboard.KEY_RETURN) {
                if (colorAnim.start()) listening = false
            } else {
                mod.keyCode = keyCode
                if (colorAnim.start()) listening = false
            }
            return true
        }
        return false
    }
}