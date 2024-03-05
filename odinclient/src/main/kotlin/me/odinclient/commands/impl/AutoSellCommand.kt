package me.odinclient.commands.impl

import com.github.stivais.commodore.parsers.impl.GreedyString
import me.odinclient.features.impl.dungeon.AutoSell.sellList
import me.odinmain.commands.CommandNode
import me.odinmain.commands.Commodore
import me.odinmain.config.Config
import me.odinmain.utils.skyblock.modMessage

object AutoSellCommand : Commodore {
    override val command: CommandNode =
        literal("autosell") {
            runs { modMessage("Usage: /autosell <add/remove/clear/list> <name>")}

            literal("add").runs { item: GreedyString ->
                val lowercase = item.string.lowercase()
                if (lowercase in sellList) return@runs modMessage("$item is already in the Auto sell list.")

                modMessage("Added $item to the Auto sell list.")
                sellList.add(lowercase)
                Config.save()
            }

            literal("remove").runs { item: GreedyString ->
                val lowercase = item.string.lowercase()
                if (lowercase !in sellList) return@runs modMessage("$item isn't in the Auto sell list.")

                modMessage("Removed $item from the Auto sell list.")
                sellList.remove(lowercase)
                Config.save()
            }

            literal("clear").runs {
                modMessage("Auto sell list cleared.")
                sellList.clear()
                Config.save()
            }

            literal("list").runs {
                if (sellList.size == 0) return@runs modMessage("Auto sell list is empty")
                modMessage("Auto sell list:\n${sellList.joinToString("\n")}")
            }
        }
}
