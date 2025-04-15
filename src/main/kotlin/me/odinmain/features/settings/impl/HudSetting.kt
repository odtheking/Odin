package me.odinmain.features.settings.impl

import me.odinmain.features.settings.Setting
import me.odinmain.utils.ui.hud.HudElement
import me.odinmain.utils.ui.hud.Render

/**
 * @author Stivais, Bonsai
 */
class HudSetting( // todo redo
    name: String,
    hud: HudElement,
    val displayToggle: Boolean = false,
    desc: String = "",
    hidden: Boolean = false
) : Setting<HudElement>(name, hidden, desc) {

    constructor(name: String, x: Float, y: Float, scale: Float = 1f, toggleable: Boolean, draw: Render) :
            this(name, HudElement(x, y, toggleable, scale, draw, name), toggleable)

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