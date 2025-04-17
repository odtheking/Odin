package me.odinclient.features.impl.floor7

import me.odinmain.features.Module
import me.odinmain.features.settings.impl.NumberSetting
import me.odinmain.utils.skyblock.modMessage

object FreezeGame : Module(
    name = "Freeze Game",
    desc = "Freezes the game when you press the keybind."
) {
    private val freezeTime by NumberSetting("Freeze Time", 8000L, 100L, 12000L, unit = "ms", desc = "The time to freeze the game for.")

    override fun onKeybind() {
        if (!enabled) return
        modMessage("Freezing game for $freezeTime ms")
        Thread.sleep(freezeTime)
    }
}