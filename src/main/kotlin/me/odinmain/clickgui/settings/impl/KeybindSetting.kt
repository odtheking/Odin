package me.odinmain.clickgui.settings.impl

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import me.odinmain.clickgui.ClickGUI.gray38
import me.odinmain.clickgui.settings.RenderableSetting
import me.odinmain.clickgui.settings.Saving
import me.odinmain.features.impl.render.ClickGUIModule
import me.odinmain.utils.render.Colors
import me.odinmain.utils.ui.isAreaHovered
import me.odinmain.utils.ui.rendering.NVGRenderer
import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse

class KeybindSetting(
    name: String,
    override val default: Keybinding,
    desc: String
) : RenderableSetting<Keybinding>(name, desc), Saving {

    constructor(name: String, defaultKeyCode: Int, desc: String = "") : this(name, Keybinding(defaultKeyCode), desc)

    override var value: Keybinding = default

    private var key: Int
        get() = value.key
        set(newKey) {
            if (newKey == key) return
            value.key = newKey
            keyNameWidth = NVGRenderer.textWidth(getKeyName(newKey), 16f, NVGRenderer.defaultFont)
        }

    private var keyNameWidth = -1f

    override fun render(x: Float, y: Float, mouseX: Float, mouseY: Float): Float {
        super.render(x, y, mouseX, mouseY)
        if (keyNameWidth < 0) keyNameWidth = NVGRenderer.textWidth(getKeyName(key), 16f, NVGRenderer.defaultFont)
        val height = getHeight()

        val rectX = x + width - 20 - keyNameWidth
        val rectY = y + height / 2f - 10f
        val rectWidth = keyNameWidth + 12f
        val rectHeight = 20f

        NVGRenderer.rect(rectX, rectY, rectWidth, rectHeight, gray38.rgba, 5f)
        NVGRenderer.hollowRect(rectX - 1, rectY - 1, rectWidth + 2f, rectHeight + 2f, 1.5f, ClickGUIModule.clickGUIColor.rgba, 4f)

        NVGRenderer.text(name, x + 6f, y + height / 2f - 8f, 16f, Colors.WHITE.rgba, NVGRenderer.defaultFont)
        NVGRenderer.text(getKeyName(key), rectX + (rectWidth - keyNameWidth) / 2, rectY + rectHeight / 2 - 8f, 16f, if (listening) Colors.MINECRAFT_YELLOW.rgba else Colors.WHITE.rgba, NVGRenderer.defaultFont)

        return height
    }

    override fun mouseClicked(mouseX: Float, mouseY: Float, mouseButton: Int): Boolean {
        if (listening) {
            key = -100 + mouseButton
            listening = false
            return true
        } else if (mouseButton == 0 && isHovered) {
            listening = true
            return true
        }
        return false
    }

    override fun keyPressed(keyCode: Int): Boolean {
        if (!listening) return false

        when (keyCode) {
            Keyboard.KEY_ESCAPE, Keyboard.KEY_BACK -> key = Keyboard.KEY_NONE
            Keyboard.KEY_RETURN -> listening = false
            else -> key = keyCode
        }

        listening = false
        return true
    }

    fun onPress(block: () -> Unit): KeybindSetting {
        value.onPress = block
        return this
    }

    override val isHovered: Boolean get() =
        isAreaHovered(lastX + width - 20 - keyNameWidth, lastY + getHeight() / 2f - 10f, keyNameWidth + 12f, 22f)

    override fun write(): JsonElement = JsonPrimitive(value.key)

    override fun read(element: JsonElement?) {
        element?.asInt?.let { value.key = it }
    }

    override fun reset() {
        value = default
    }

    fun getKeyName(key: Int): String {
        return if (key > 0) Keyboard.getKeyName(key) ?: "Err"
        else if (key < 0) Mouse.getButtonName(key + 100)
        else "None"
    }
}

class Keybinding(var key: Int) {

    /**
     * Intended to active when keybind is pressed.
     */
    var onPress: (() -> Unit)? = null

    /**
     * @return `true` if [key] is held down.
     */
    fun isDown(): Boolean {
        return if (key == 0) false else (if (key < 0) Mouse.isButtonDown(key + 100) else Keyboard.isKeyDown(key))
    }
}