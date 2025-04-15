package me.odinmain.features.settings.impl

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import me.odinmain.features.settings.Saving
import me.odinmain.features.settings.Setting
import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse

class KeybindSetting(
    name: String,
    override val default: Keybinding,
    desc: String,
    hidden: Boolean = false
) : Setting<Keybinding>(name, hidden, desc), Saving {

    constructor(name: String, key: Int, description: String, hidden: Boolean = false) : this(name, Keybinding(key), description, hidden)

    override var value: Keybinding = default

    override fun write(): JsonElement {
        return JsonPrimitive(value.key)
    }

    override fun read(element: JsonElement?) {
        element?.asInt?.let {
            value.key = it
        }
    }

    /**
     * Action to do, when keybinding is pressed
     *
     * Note: Action is always invoked, even if module isn't enabled.
     */
    fun onPress(block: () -> Unit): KeybindSetting {
        value.onPress = block
        return this
    }

    override fun reset() {
        value.key = default.key
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