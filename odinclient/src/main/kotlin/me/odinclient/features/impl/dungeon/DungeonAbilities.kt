package me.odinclient.features.impl.dungeon

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.KeybindSetting
import me.odinmain.features.settings.impl.Keybinding
import me.odinmain.utils.runIn
import me.odinmain.utils.skyblock.PlayerUtils
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.skyblock.modMessage
import org.lwjgl.input.Keyboard

object DungeonAbilities : Module(
    name = "Dungeon Abilities",
    description = "Automatically uses your ability in dungeons.",
    category = Category.DUNGEON
) {
    private val abilityKeybind: Keybinding by KeybindSetting("Ability Keybind", Keyboard.KEY_NONE, description = "Keybind to use your ability.").onPress {
        if (!DungeonUtils.inDungeons || !enabled) return@onPress
        PlayerUtils.dropItem(true)
    }

    init {
        onMessage("⚠ Maxor is enraged! ⚠", false) {
            PlayerUtils.dropItem()
            modMessage("§cUsing ult!")
        }

        onMessage("[BOSS] Goldor: You have done it, you destroyed the factory…", false) {
            PlayerUtils.dropItem()
            modMessage("§cUsing ult!")
        }

        onMessage("[BOSS] Sadan: My giants! Unleashed!", false) {
            runIn(20) {
                PlayerUtils.dropItem()
                modMessage("§cUsing ult!")
            }
        }
    }
}