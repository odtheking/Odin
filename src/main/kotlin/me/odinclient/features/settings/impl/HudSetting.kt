package me.odinclient.features.settings.impl

import me.odinclient.features.ModuleManager
import me.odinclient.features.settings.Setting
import me.odinclient.ui.hud.BaseHud

class HudSetting (
    name: String,
    hud: BaseHud,
    override val default: Boolean = false,
    hidden: Boolean = false,
    description: String? = null,
) : Setting<Boolean>(name, hidden, description) {

    var isEnabled = false

    override var value: Boolean = default
        set (value) {
            field = processInput(value)
        }

    init {
        ModuleManager.huds.add(Pair(hud, this))
    }
}