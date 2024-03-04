package me.odinmain.features.settings.impl

import me.odinmain.features.settings.Setting

/**
 * A setting intended to show or hide other settings in the GUI.
 *
 * @author Bonsai
 */
class DropdownSetting (
    name: String,
    override val default: Boolean = false
): Setting<Boolean>(name, false, "") {

    override var value: Boolean = default

    var enabled: Boolean by this::value
}