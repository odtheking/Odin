package me.odinclient.commands.impl

import com.github.stivais.commodore.parsers.impl.GreedyString
import me.odinmain.OdinMain
import me.odinmain.commands.CommandNode
import me.odinmain.commands.Commodore
import me.odinmain.config.MiscConfig
import me.odinmain.utils.skyblock.modMessage

object AutoSellCommand : Commodore {
    override val command: CommandNode =
        literal("autosell") {
            requires {
                !OdinMain.onLegitVersion
            }

            runs { modMessage("Usage: /autosell <add/remove/clear/list> <name>")}

            literal("add").runs { item: GreedyString ->
                val lowercase = item.string.lowercase()
                if (lowercase in MiscConfig.autoSell) return@runs modMessage("$item is already in the Auto sell list.")

                modMessage("Added $item to the Auto sell list.")
                MiscConfig.autoSell.add(lowercase)
                MiscConfig.saveAllConfigs()
            }

            literal("remove").runs { item: GreedyString ->
                val lowercase = item.string.lowercase()
                if (lowercase !in MiscConfig.autoSell) return@runs modMessage("$item isn't in the Auto sell list.")

                modMessage("Removed $item from the Auto sell list.")
                MiscConfig.autoSell.remove(lowercase)
                MiscConfig.saveAllConfigs()
            }

            literal("clear").runs {
                modMessage("Auto sell list cleared.")
                MiscConfig.autoSell.clear()
                MiscConfig.saveAllConfigs()
            }

            literal("list").runs {
                if (MiscConfig.autoSell.size == 0) return@runs modMessage("Auto sell list is empty")
                modMessage("Auto sell list:\n${MiscConfig.autoSell.joinToString("\n")}")
            }
        }
}
