package me.odinmain.features.settings.impl

import me.odinmain.features.settings.Setting

/**
 * Setting that gets ran when clicked.
 *
 * @author Aton
 */
class ActionSetting(
    name: String,
    desc: String,
    hidden: Boolean = false,
    override val default: () -> Unit = {}
) : Setting<() -> Unit>(name, hidden, desc) {

    override var value: () -> Unit = default

    var action: () -> Unit by this::value
}