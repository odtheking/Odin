package com.odtheking.odin.clickgui.settings.impl

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.mojang.blaze3d.platform.InputConstants
import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.clickgui.ClickGUI.gray38
import com.odtheking.odin.clickgui.settings.RenderableSetting
import com.odtheking.odin.clickgui.settings.Saving
import com.odtheking.odin.features.impl.render.ClickGUIModule
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.ui.isAreaHovered
import com.odtheking.odin.utils.ui.rendering.NVGRenderer
import net.minecraft.client.input.KeyEvent
import net.minecraft.client.input.MouseButtonEvent
import org.lwjgl.glfw.GLFW

class KeybindSetting(
    name: String,
    override val default: InputConstants.Key,
    desc: String
) : RenderableSetting<InputConstants.Key>(name, desc), Saving {

    constructor(name: String, defaultKeyCode: Int, desc: String = "") : this(name, InputConstants.Type.KEYSYM.getOrCreate(defaultKeyCode), desc)

    override var value: InputConstants.Key = default
    var onPress: (() -> Unit)? = null
    private var keyNameWidth = -1f

    private var key: InputConstants.Key
        get() = value
        set(newKey) {
            if (newKey == value) return
            value = newKey
            keyNameWidth = NVGRenderer.textWidth(value.displayName.string, 16f, NVGRenderer.defaultFont)
        }

    override fun render(x: Float, y: Float, mouseX: Float, mouseY: Float): Float {
        super.render(x, y, mouseX, mouseY)
        if (keyNameWidth < 0) keyNameWidth = NVGRenderer.textWidth(value.displayName.string, 16f, NVGRenderer.defaultFont)
        val height = getHeight()

        val rectX = x + width - 20 - keyNameWidth
        val rectY = y + height / 2f - 10f
        val rectWidth = keyNameWidth + 12f
        val rectHeight = 20f

        NVGRenderer.rect(rectX, rectY, rectWidth, rectHeight, gray38.rgba, 5f)
        NVGRenderer.hollowRect(rectX - 1, rectY - 1, rectWidth + 2f, rectHeight + 2f, 1.5f, ClickGUIModule.clickGUIColor.rgba, 4f)

        NVGRenderer.text(name, x + 6f, y + height / 2f - 8f, 16f, Colors.WHITE.rgba, NVGRenderer.defaultFont)
        NVGRenderer.text(value.displayName.string, rectX + (rectWidth - keyNameWidth) / 2, rectY + rectHeight / 2 - 8f, 16f, if (listening) Colors.MINECRAFT_YELLOW.rgba else Colors.WHITE.rgba, NVGRenderer.defaultFont)

        return height
    }

    override fun mouseClicked(mouseX: Float, mouseY: Float, click: MouseButtonEvent): Boolean {
        if (listening) {
            key = InputConstants.Type.MOUSE.getOrCreate(click.button())
            listening = false
            return true
        } else if (click.button() == 0 && isHovered) {
            listening = true
            return true
        }
        return false
    }

    override fun keyPressed(input: KeyEvent): Boolean {
        if (!listening) return false

        when (input.key) {
            GLFW.GLFW_KEY_ESCAPE, GLFW.GLFW_KEY_BACKSPACE -> key = InputConstants.UNKNOWN
            GLFW.GLFW_KEY_ENTER -> listening = false
            else -> key = InputConstants.getKey(input)
        }

        listening = false
        return true
    }

    fun onPress(block: () -> Unit): KeybindSetting {
        onPress = block
        return this
    }

    fun isDown(): Boolean =
        value != InputConstants.UNKNOWN && InputConstants.isKeyDown(mc.window, value.value)

    override val isHovered: Boolean
        get() =
            isAreaHovered(lastX + width - 20 - keyNameWidth, lastY + getHeight() / 2f - 10f, keyNameWidth + 12f, 22f, true)

    override fun write(gson: Gson): JsonElement = JsonPrimitive(value.name)

    override fun read(element: JsonElement, gson: Gson) {
        element.asString?.let { value = InputConstants.getKey(it) }
    }

    override fun reset() {
        value = default
    }
}
