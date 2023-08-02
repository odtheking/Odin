package me.odinclient.features.settings.impl

import me.odinclient.features.ModuleManager
import me.odinclient.features.settings.Setting
import me.odinclient.ui.hud.BaseHud
import me.odinclient.ui.hud.HudData

class HudSetting (
    name: String,
    val hud: BaseHud,
    override val default: HudData = HudData(0f, 0f, 1f, false),
    hidden: Boolean = false,
    description: String? = null,
) : Setting<HudData>(name, hidden, description) {

    override var value: HudData = default
        set (value) {
            field = processInput(value)
        }

    init {
        hud.x = value.x
        hud.y = value.y
        hud.scale = value.scale

        ModuleManager.huds.add(Pair(hud, this))
    }
}