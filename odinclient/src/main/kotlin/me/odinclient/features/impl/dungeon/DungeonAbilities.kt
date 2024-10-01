package me.odinclient.features.impl.dungeon

import me.odinclient.utils.skyblock.PlayerUtils.dropItem
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.*
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.skyblock.modMessage
import org.lwjgl.input.Keyboard

object DungeonAbilities : Module(
    name = "Dungeon Abilities",
    description = "Automatically uses your ability in dungeons.",
    category = Category.DUNGEON
) {
    private val autoUlt: Boolean by BooleanSetting("Auto Ult", default = false, description = "Automatically uses your ultimate ability whenever needed.")
    private val abilityKeybind: Keybinding by KeybindSetting("Ability Keybind", Keyboard.KEY_NONE, description = "Keybind to use your ability.").onPress {
        if (!DungeonUtils.inDungeons || !enabled) return@onPress
        dropItem(dropAll = true)
    }

    init {
        onMessage("⚠ Maxor is enraged! ⚠", false, { enabled && autoUlt }) {
            dropItem()
            modMessage("§aUsing ult!")
        }

        onMessage("[BOSS] Goldor: You have done it, you destroyed the factory…", false, { enabled && autoUlt }) {
            dropItem()
            modMessage("§aUsing ult!")
        }

        onMessage("[BOSS] Sadan: My giants! Unleashed!", false, { enabled && autoUlt }) {
            dropItem(ticks = 25)
            modMessage("§aUsing ult!")
        }
    }
}