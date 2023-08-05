package me.odinclient.features.settings.impl

import me.odinclient.features.settings.Setting

class ActionSetting(
    name: String,
    hidden: Boolean = false,
    description: String = "",
    override val default: () -> Unit = {}
) : Setting<() -> Unit>(name, hidden, description) {

    override var value: () -> Unit = default

    var action: () -> Unit by this::value

    fun doAction() {
        action()
    }
}