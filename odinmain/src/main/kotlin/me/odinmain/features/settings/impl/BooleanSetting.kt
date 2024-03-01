package me.odinmain.features.settings.impl

import me.odinmain.features.settings.Setting

/**
 * A true-false setting.
 * @author Aton
 */
class BooleanSetting (
    name: String,
    override val default: Boolean = false,
    hidden: Boolean = false,
    description: String = "",
): Setting<Boolean>(name, hidden, description) {

    override var value: Boolean = default

    override fun update(configSetting: Setting<*>) {
        value = (configSetting as BooleanSetting).enabled
    }

    var enabled: Boolean by this::value

    fun toggle() {
        enabled = !enabled
    }
}