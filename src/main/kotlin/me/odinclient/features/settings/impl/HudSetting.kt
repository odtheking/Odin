package me.odinclient.features.settings.impl


import me.odinclient.features.settings.Setting
import me.odinclient.ui.hud.HudElement

/**
 * Used for just rendering
 */
class HudSetting(
    name: String,
    val displayToggle: Boolean = false,
    override val default: HudElement,
    hidden: Boolean = false,
    description: String? = null,
) : Setting<HudElement>(name, hidden, description) {

    /**
     * Not intended to be used.
     */
    override var value: HudElement = default
}