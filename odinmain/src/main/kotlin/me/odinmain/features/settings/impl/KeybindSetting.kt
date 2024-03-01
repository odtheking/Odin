package me.odinmain.features.settings.impl

import me.odinmain.features.settings.Setting
import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse

class KeybindSetting(
    name: String,
    override val default: Keybinding,
    description: String,
    hidden: Boolean = false
) : Setting<Keybinding>(name, hidden, description) {

    constructor(name: String, key: Int, description: String, hidden: Boolean = false) : this(name, Keybinding(key), description, hidden)

    override var value: Keybinding = default

    override fun update(configSetting: Setting<*>) {
        value.key = (configSetting as NumberSetting).valueDouble.toInt()
    }

    fun onPress(block: () -> Unit): KeybindSetting {
        value.onPress = block
        return this
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
        return key != 0 && (Keyboard.isKeyDown(key) || Mouse.isButtonDown(key + 100))
    }
}