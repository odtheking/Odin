package me.odinclient.commands.impl

import com.github.stivais.commodore.utils.GreedyString
import me.odinclient.features.impl.dungeon.AutoSell.sellList
import me.odinmain.commands.commodore
import me.odinmain.config.Config
import me.odinmain.utils.skyblock.modMessage

val autoSellCommand = commodore("autosell") {
    runs { modMessage("Usage:\n /autosell <add/remove> <name>\n /autosell <clear/list>")}

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
