package me.odinmain.features.settings.impl

import me.odinmain.features.settings.Setting

/**
 * A setting used to show or hide other settings.
 * @author Bonsai
 */
class DropdownSetting (
    name: String,
    override val default: Boolean = false
): Setting<Boolean>(name, false, "") {

    override var value: Boolean = default

    override fun update(configSetting: Setting<*>) {
        value = (configSetting as BooleanSetting).enabled
    }

    var enabled: Boolean by this::value

    fun toggle() {
        enabled = !enabled
    }
}