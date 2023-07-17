package me.odinclient.features.settings.impl

import me.odinclient.features.settings.Setting

/**
 * A boolean setting for Modules, can be represented by a checkbox in the gui.
 *
 * @author Aton
 */
class BooleanSetting (
    name: String,
    override val default: Boolean = false,
    hidden: Boolean = false,
    description: String? = null,
): Setting<Boolean>(name, hidden, description) {

    override var value: Boolean = default
        set (value) {
            field = processInput(value)

        }

    var enabled: Boolean by this::value

    fun toggle() {
        enabled = !enabled
    }
}