package me.odinclient.features.impl.floor7

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.NumberSetting
import me.odinmain.utils.skyblock.modMessage

object FreezeGame : Module(
    name = "Freeze Game",
    description = "Freezes the game when you press the keybind.",
    category = Category.FLOOR7,
    tag = TagType.NEW
) {
    private val freezeTime: Long by NumberSetting("Freeze Time", 8000L, 100L, 12000L)

    override fun onKeybind() {
        if (!enabled) return
        modMessage("Freezing game for $freezeTime ms")
        Thread.sleep(freezeTime)
    }
}