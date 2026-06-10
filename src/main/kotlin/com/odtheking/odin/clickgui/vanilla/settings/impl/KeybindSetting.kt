package com.odtheking.odin.clickgui.vanilla.settings.impl

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.mojang.blaze3d.platform.InputConstants
import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.clickgui.settings.ClickGUI.gray38
import com.odtheking.odin.clickgui.settings.Saving
import com.odtheking.odin.clickgui.vanilla.VanillaRenderableSetting
import com.odtheking.odin.features.impl.render.ClickGUIModule
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.render.roundedFill
import com.odtheking.odin.utils.ui.isAreaHovered
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.input.KeyEvent
import net.minecraft.client.input.MouseButtonEvent
import org.lwjgl.glfw.GLFW

class KeybindSetting(
    name: String,
    override val default: InputConstants.Key,
    desc: String
) : VanillaRenderableSetting<InputConstants.Key>(name, desc), Saving {

    constructor(name: String, defaultKeyCode: Int, desc: String = "") : this(name, InputConstants.Type.KEYSYM.getOrCreate(defaultKeyCode), desc)

    override var value: InputConstants.Key = default
    var onPress: (() -> Unit)? = null

    override fun render(graphics: GuiGraphics, x: Int, y: Int, mouseX: Int, mouseY: Int): Int {
        super.render(graphics, x, y, mouseX, mouseY)
        val height = getHeight()

        graphics.drawString(mc.font, name, x + 6, y + height / 2 - 4, Colors.WHITE.rgba, false)

        val keyName = value.displayName.string
        val keyWidth = mc.font.width(keyName)
        val rectX = x + width - 17 - keyWidth
        val rectY = y + height / 2 - 8
        val rectW = keyWidth + 12

        graphics.roundedFill(rectX, rectY, rectX + rectW, rectY + 16, gray38.rgba, 5, ClickGUIModule.clickGUIColor.rgba, 1.5f)

        val keyColor = if (listening) Colors.MINECRAFT_YELLOW.rgba else Colors.WHITE.rgba
        graphics.drawString(mc.font, keyName, rectX + (rectW - keyWidth) / 2, y + height / 2 - 4, keyColor, false)

        return height
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, click: MouseButtonEvent): Boolean {
        if (listening) {
            value = InputConstants.Type.MOUSE.getOrCreate(click.button())
            listening = false
            return true
        }
        if (click.button() == 0 && isHovered) {
            listening = true
            return true
        }
        return false
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, click: MouseButtonEvent) {
        if (click.button() == 0 && !isHovered) listening = false
    }

    override fun keyPressed(input: KeyEvent): Boolean {
        if (!listening) return false
        when (input.key) {
            GLFW.GLFW_KEY_ESCAPE, GLFW.GLFW_KEY_BACKSPACE -> value = InputConstants.UNKNOWN
            GLFW.GLFW_KEY_ENTER -> {}
            else -> value = InputConstants.getKey(input)
        }
        listening = false
        return true
    }

    fun onPress(block: () -> Unit): KeybindSetting {
        onPress = block
        return this
    }

    override val isHovered: Boolean
        get() {
            val keyWidth = mc.font.width(value.displayName.string)
            return isAreaHovered(lastX + width - 17 - keyWidth, lastY + getHeight() / 2 - 8, keyWidth + 12, 16)
        }

    override fun write(gson: Gson): JsonElement = JsonPrimitive(value.name)

    override fun read(element: JsonElement, gson: Gson) {
        element.asString?.let { value = InputConstants.getKey(it) }
    }

    override fun reset() {
        value = default
    }

    companion object {
        fun InputConstants.Key.isDown(): Boolean {
            val window = mc.window
            return if (value > 7) InputConstants.isKeyDown(window, value)
            else GLFW.glfwGetMouseButton(window.handle(), value) == GLFW.GLFW_PRESS
        }
    }
}
