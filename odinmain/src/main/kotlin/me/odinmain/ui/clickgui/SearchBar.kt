package me.odinmain.ui.clickgui

import me.odinmain.OdinMain.mc
import me.odinmain.ui.clickgui.elements.menu.ElementTextField
import me.odinmain.ui.clickgui.util.ColorUtil
import me.odinmain.ui.clickgui.util.ColorUtil.buttonColor
import me.odinmain.ui.clickgui.util.ColorUtil.withAlpha
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.gui.*
import me.odinmain.utils.render.gui.animations.impl.ColorAnimation
import net.minecraft.client.renderer.GlStateManager
import org.lwjgl.input.Keyboard

object SearchBar {

    var currentSearch = ""
    private var listening = false
    private val isHovered get() = MouseUtils.isAreaHovered(mc.displayWidth / 2f - 200f, mc.displayHeight - 100f, 400f, 30f)
    private val colorAnim = ColorAnimation(100)

    fun draw() {
        GlStateManager.pushMatrix()
        translate(mc.displayWidth / 4f, mc.displayHeight / 2f - 50f)
        roundedRectangle(-200f, 0f, 400f, 30f, ColorUtil.moduleButtonColor, 9f)
        if (listening || colorAnim.isAnimating()) {
            val color = colorAnim.get(ColorUtil.clickGUIColor, buttonColor, listening)
            rectangleOutline(-202f, -1f, 404f, 32f, color, 9f,3f)
        }
        if (currentSearch.isEmpty()) {
            text("Search here...", 0f, 16f, Color.WHITE.withAlpha(0.5f), 20f, Fonts.REGULAR, TextAlign.Middle)
        } else text(currentSearch, 0f, 16f, Color.WHITE, 20f, Fonts.REGULAR, TextAlign.Middle)
        translate(-mc.displayWidth / 4f, -mc.displayHeight / 4f + 200f)
        GlStateManager.popMatrix()
    }

    fun mouseClicked(mouseButton: Int): Boolean {
        if (mouseButton == 0 && isHovered) {
            if (colorAnim.start()) listening = !listening
            return true
        } else if (listening) {
            if (colorAnim.start()) listening = false
        }
        return false
    }

    fun keyTyped(typedChar: Char, keyCode: Int): Boolean {
        if (listening) {
            when (keyCode) {
                Keyboard.KEY_ESCAPE, Keyboard.KEY_NUMPADENTER, Keyboard.KEY_RETURN -> if (colorAnim.start()) listening = false
                Keyboard.KEY_BACK -> currentSearch = currentSearch.dropLast(1)
                !in ElementTextField.keyBlackList -> currentSearch += typedChar.toString()
            }
            if (currentSearch.length > "Auto-Renew Hollows Pass".length) currentSearch = currentSearch.dropLast(1)
            return true
        }
        return false
    }
}