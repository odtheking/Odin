package me.odinclient.features.settings.impl

import me.odinclient.features.settings.Setting

/**
 * A true-false setting.
 * @author Aton, Bonsai
 */
class DualSetting (
    name: String,
    val left: String,
    val right: String,
    override val default: Boolean = false,
    hidden: Boolean = false,
    description: String = "",
): Setting<Boolean>(name, hidden, description) {

    override var value: Boolean = default
        set (value) {
            field = processInput(value)
        }

    var enabled: Boolean by this::value
}