package com.odtheking.odin.clickgui.settings.impl

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.mojang.blaze3d.platform.InputConstants
import com.odtheking.odin.OdinMod
import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.clickgui.GuiTheme
import com.odtheking.odin.clickgui.settings.RenderableSetting
import com.odtheking.odin.clickgui.settings.Saving
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.render.roundedRectOutlined
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper
import net.minecraft.client.KeyMapping
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.input.KeyEvent
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.resources.Identifier
import org.lwjgl.glfw.GLFW

class KeybindSetting(
    name: String,
    override val default: InputConstants.Key,
    desc: String
) : RenderableSetting<InputConstants.Key>(name, desc), Saving {

    constructor(name: String, defaultKeyCode: Int, desc: String = "") : this(name, InputConstants.Type.KEYSYM.getOrCreate(defaultKeyCode), desc)

    override var value: InputConstants.Key
        get() = mapping?.let { KeyMappingHelper.getBoundKeyOf(it) } ?: pending
        set(key) {
            pending = key
            val mapping = mapping ?: return
            if (mapping.matches(key)) return
            mapping.setKey(key)
            KeyMapping.resetMapping()
            pendingOptionsSave = true
        }

    private var mapping: KeyMapping? = null
    private var pending: InputConstants.Key = default
    val boundKey: InputConstants.Key get() = value

    var onPress: (() -> Unit)? = null
    private var listening = false

    private var namedKey: InputConstants.Key? = null
    private var keyName = ""

    private val boundName: String
        get() {
            val key = value
            if (key !== namedKey) {
                namedKey = key
                keyName = key.displayName.string
            }
            return keyName
        }

    override val clickButtons: IntArray get() = if (listening) ALL_MOUSE_BUTTONS else BOTH_BUTTONS

    fun registerKeyMapping(owner: String) {
        if (mapping != null) return
        val label = if (name == "Keybind") owner else "$owner ($name)"
        mapping = KeyMappingHelper.registerKeyMapping(KeyMapping(label, pending.type, pending.value, KEYBIND_CATEGORY))
    }

    fun onPress(block: () -> Unit): KeybindSetting {
        onPress = block
        return this
    }

    override fun render(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int) {
        if (listening && !isFocused) listening = false

        drawLabel(graphics)

        val boxWidth = mc.font.width(boundName) + PADDING * 2
        val boxX = x + width - RIGHT_PAD - boxWidth
        val boxY = y + (height - BOX_HEIGHT) / 2

        graphics.roundedRectOutlined(boxX, boxY, boxX + boxWidth, boxY + BOX_HEIGHT, GuiTheme.surface.rgba, GuiTheme.accent.rgba, 1f, GuiTheme.RADIUS)

        val color = if (listening) Colors.MINECRAFT_YELLOW.rgba else Colors.WHITE.rgba
        graphics.text(mc.font, boundName, boxX + PADDING, GuiTheme.textY(boxY, BOX_HEIGHT), color, false)
    }

    override fun onClick(event: MouseButtonEvent, doubleClick: Boolean) {
        if (listening) {
            value = InputConstants.Type.MOUSE.getOrCreate(event.button())
            listening = false
        } else if (event.button() == LEFT) listening = true
    }

    override fun keyPressed(event: KeyEvent): Boolean {
        if (!listening) return false
        when (event.key) {
            GLFW.GLFW_KEY_ESCAPE, GLFW.GLFW_KEY_BACKSPACE -> value = InputConstants.UNKNOWN
            GLFW.GLFW_KEY_ENTER -> Unit
            else -> value = InputConstants.getKey(event)
        }
        listening = false
        return true
    }

    override fun release() {
        listening = false
    }

    override fun write(gson: Gson): JsonElement = JsonPrimitive(value.name)

    override fun read(element: JsonElement, gson: Gson) {
        val saved = element.asString?.let(InputConstants::getKey) ?: return
        if (mapping?.isDefault != false) value = saved
    }

    override fun reset() {
        value = default
    }

    companion object {
        private const val BOX_HEIGHT = 16
        private const val PADDING = 6
        private const val RIGHT_PAD = 5

        private val ALL_MOUSE_BUTTONS = IntArray(GLFW.GLFW_MOUSE_BUTTON_LAST + 1) { it }

        private val KEYBIND_CATEGORY: KeyMapping.Category =
            KeyMapping.Category.register(Identifier.fromNamespaceAndPath(OdinMod.MOD_ID, "keybinds"))

        private var pendingOptionsSave = false

        fun saveOptionsIfChanged() {
            if (!pendingOptionsSave) return
            pendingOptionsSave = false
            mc.options.save()
        }

        fun InputConstants.Key.isDown(): Boolean = when {
            this == InputConstants.UNKNOWN -> false
            type == InputConstants.Type.MOUSE -> GLFW.glfwGetMouseButton(mc.window.handle(), value) == GLFW.GLFW_PRESS
            value >= GLFW.GLFW_KEY_SPACE -> InputConstants.isKeyDown(mc.window, value)
            else -> false
        }
    }
}
