package me.odinclient.features.settings.impl

import me.odinclient.features.settings.Setting
import me.odinclient.ui.hud.HudElement
import me.odinclient.ui.hud.Render

/**
 * @author Stivais, Bonsai
 */
class HudSetting(
    name: String,
    hud: HudElement,
    val displayToggle: Boolean = false,
    hidden: Boolean = false,
    description: String = "",
) : Setting<HudElement>(name, hidden, description) {

    constructor(name: String, x: Float, y: Float, scale: Float = 1f, toggleable: Boolean, draw: Render) :
            this(name, HudElement(x, y, scale, draw), toggleable)

    override val default: HudElement = hud

    /**
     * Not intended to be used.
     */
    override var value: HudElement = default

    inline var enabled: Boolean
        get() = value.enabled
        set(value) {
            this.value.enabled = value
        }

    init {
        if (!displayToggle) value.enabled = true
    }
}