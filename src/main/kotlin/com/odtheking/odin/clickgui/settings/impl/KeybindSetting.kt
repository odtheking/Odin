package com.odtheking.odin.clickgui.settings.impl

import androidx.compose.runtime.*
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.mojang.blaze3d.platform.InputConstants
import com.odtheking.odin.OdinMod
import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.clickgui.settings.RenderableSetting
import com.odtheking.odin.clickgui.settings.Saving
import com.odtheking.odin.clickgui.ui.Pill
import com.odtheking.odin.clickgui.ui.SettingRow
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.ui.compose.onFocusChanged
import com.odtheking.odin.utils.ui.compose.onKey
import com.odtheking.odin.utils.ui.compose.pointerInput
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper
import net.minecraft.client.KeyMapping
import net.minecraft.resources.Identifier

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

    fun registerKeyMapping(owner: String) {
        if (mapping != null) return
        val label = if (name == "Keybind") owner else "$owner ($name)"
        mapping = KeyMappingHelper.registerKeyMapping(KeyMapping(label, pending.type, pending.value, KEYBIND_CATEGORY))
    }

    fun onPress(block: () -> Unit): KeybindSetting {
        onPress = block
        return this
    }

    @Composable
    override fun Content() {
        var listening by remember { mutableStateOf(false) }

        SettingRow {
            Pill(
                { boundName },
                color = { if (listening) Colors.MINECRAFT_YELLOW.rgba else Colors.WHITE.rgba },
            )
                .pointerInput { event ->
                    when {
                        listening -> {
                            value = InputConstants.Type.MOUSE.getOrCreate(event.button())
                            listening = false
                        }
                        event.button() == InputConstants.MOUSE_BUTTON_LEFT -> listening = true
                        else -> return@pointerInput event.button() == InputConstants.MOUSE_BUTTON_RIGHT
                    }
                    true
                }
                .onKey { event ->
                    if (!listening) return@onKey false
                    when (event.key) {
                        InputConstants.KEY_ESCAPE, InputConstants.KEY_BACKSPACE -> value = InputConstants.UNKNOWN
                        InputConstants.KEY_RETURN -> Unit
                        else -> value = InputConstants.getKey(event)
                    }
                    listening = false
                    true
                }
                .onFocusChanged { if (!it) listening = false }
        }
    }

    override fun write(gson: Gson): JsonElement = JsonPrimitive(value.name)

    override fun read(element: JsonElement, gson: Gson) {
        val saved = element.asString?.let(InputConstants::getKey) ?: return
        if (mapping?.isDefault != false) value = saved
    }

    companion object {
        private val KEYBIND_CATEGORY: KeyMapping.Category =
            KeyMapping.Category.register(Identifier.fromNamespaceAndPath(OdinMod.MOD_ID, "keybinds"))

        private var pendingOptionsSave = false

        fun saveOptionsIfChanged() {
            if (!pendingOptionsSave) return
            pendingOptionsSave = false
            mc.execute {
                mc.options.save()
            }
        }

        fun InputConstants.Key.isDown(): Boolean = InputConstants.isKeyDown(mc.window, value)
    }
}
