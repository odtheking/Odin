package me.odinclient.features.impl.dungeon

import me.odinclient.utils.skyblock.PlayerUtils
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.utils.skyblock.dungeon.DungeonUtils

object AbilityKeybind : Module(
    name = "Ability Keybind",
    description = "Activates your class ability when you press the keybind.",
    category = Category.DUNGEON
) {
    override fun onKeybind() {
        if (!DungeonUtils.inDungeons || !enabled) return
        PlayerUtils.dropAll()
    }
}