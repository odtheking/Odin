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
    private val autoUlt by BooleanSetting("Auto Ult", default = false, description = "Automatically uses your ultimate ability whenever needed.")
    private val abilityKeybind by KeybindSetting("Ability Keybind", Keyboard.KEY_NONE, description = "Keybind to use your ability.").onPress {
        if (!DungeonUtils.inDungeons || !enabled) return@onPress
        dropItem(dropAll = true)
    }

    init {
        onMessage(Regex("⚠ Maxor is enraged! ⚠"), { enabled && autoUlt }) {
            dropItem()
            modMessage("§aUsing ult!")
        }

        onMessage(Regex("\\[BOSS] Goldor: You have done it, you destroyed the factory…"), { enabled && autoUlt }) {
            dropItem()
            modMessage("§aUsing ult!")
        }

        onMessage(Regex("\\[BOSS] Sadan: My giants! Unleashed!"), { enabled && autoUlt }) {
            dropItem(delay = 25)
            modMessage("§aUsing ult!")
        }
    }
}