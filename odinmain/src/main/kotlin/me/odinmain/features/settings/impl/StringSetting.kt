package me.odinmain.features.settings.impl

import me.odinmain.features.settings.Setting


/**
 * Setting that lets you type a string.
 * @author Aton
 */
class StringSetting(
    name: String,
    override val default: String = "",
    var length: Int = 20,
    hidden: Boolean = false,
    description: String = "",
) : Setting<String>(name, hidden, description) {

    override var value: String = default
        set(value) {
            field = if (value.length <= length) value else return
        }

    override fun update(configSetting: Setting<*>) {
        value = (configSetting as StringSetting).text
    }

    var text: String by this::value
}