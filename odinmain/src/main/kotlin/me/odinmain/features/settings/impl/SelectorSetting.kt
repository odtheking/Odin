package me.odinmain.features.settings.impl

import me.odinmain.features.settings.Setting


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

    override fun update(configSetting: Setting<*>) {
        selected = (configSetting as StringSetting).text
    }

    var index: Int = optionIndex(defaultSelected)
        set(value) {
            field = if (value > options.size - 1) 0 else if (value < 0) options.size - 1 else value
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