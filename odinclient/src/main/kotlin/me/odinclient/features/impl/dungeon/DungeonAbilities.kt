package me.odinclient.features.impl.dungeon

import me.odinclient.utils.skyblock.PlayerUtils.dropItem
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.KeybindSetting
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.skyblock.modMessage
import org.lwjgl.input.Keyboard

object DungeonAbilities : Module(
    name = "Dungeon Abilities",
    desc = "Automatically uses your ability in dungeons."
) {
    private val autoUlt by BooleanSetting("Auto Ult", false, desc = "Automatically uses your ultimate ability whenever needed.")
    private val abilityKeybind by KeybindSetting("Ability Keybind", Keyboard.KEY_NONE, desc = "Keybind to use your ability.").onPress {
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