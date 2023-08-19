package me.odinclient.features.settings.impl

import me.odinclient.features.settings.Setting

/**
 * Setting that lets you pick between an array of strings.
 * @author Aton
 */
class SelectorSetting(
    name: String,
    defaultSelected: String,
    var options: ArrayList<String>,
    hidden: Boolean = false,
    description: String = "",
) : Setting<Int>(name, hidden, description) {

    override val default: Int = optionIndex(defaultSelected)

    override var value: Int
        get() = index
        set(value) {
            index = value
        }

    var index: Int = optionIndex(defaultSelected)
        set(value) {
            val newVal = processInput(value)
            field = if (newVal > options.size - 1) 0 else if (newVal < 0) options.size - 1 else newVal
        }

    var selected: String
        set(value) {
            index = optionIndex(value)
        }
        get() {
            return options[index]
        }

    private fun optionIndex(string: String): Int =
        options.map { it.lowercase() }.indexOf(string.lowercase()).coerceIn(0, options.size - 1)

}