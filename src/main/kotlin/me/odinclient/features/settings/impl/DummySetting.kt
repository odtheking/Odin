package me.odinclient.features.settings.impl

import me.odinclient.features.settings.Setting

/**
 * Placeholder setting class.
 * @author Aton
 */
class DummySetting(
    name: String,
    hidden: Boolean = false,
    description: String = "",
) : Setting<Any?>(name, hidden, description) {
    /**
     * Always is null.
     * Not intended to be used.
     */
    override val default: Any?
        get() = null

    /**
     * Always is null.
     * Not intended to be used.
     */
    override var value: Any?
        get() = null
        set(_) {}
}