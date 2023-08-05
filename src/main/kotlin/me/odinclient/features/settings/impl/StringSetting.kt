package me.odinclient.features.settings.impl

import me.odinclient.features.settings.Setting

class StringSetting(
    name: String,
    override val default: String = "",
    var length: Int = 20,
    hidden: Boolean = false,
    description: String = "",
) : Setting<String>(name, hidden, description) {

    override var value: String = default
        set(newStr) {
            val tempStr = processInput(newStr)
            field = if (tempStr.length <= length) tempStr else return
        }

    var text: String by this::value
}