package me.odinclient.commands.impl

import com.github.stivais.commodore.Commodore
import com.github.stivais.commodore.utils.GreedyString
import me.odinclient.features.impl.dungeon.AutoSell.sellList
import me.odinmain.OdinMain.mc
import me.odinmain.config.Config
import me.odinmain.utils.skyblock.modMessage
import me.odinmain.utils.skyblock.unformattedName

val autoSellCommand = Commodore("autosell") {

    literal("add").runs { item: GreedyString? ->
        val lowercase = item?.string?.lowercase() ?: mc.thePlayer?.heldItem?.unformattedName?.lowercase() ?: return@runs modMessage("Either hold an item or write an item name to be added to autosell.")
        if (lowercase in sellList) return@runs modMessage("$lowercase is already in the Auto sell list.")

        modMessage("Added $lowercase to the Auto sell list.")
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
        if (sellList.isEmpty()) return@runs modMessage("Auto sell list is empty")
        val chunkedList = sellList.chunked(10)
        modMessage("Auto sell list:\n${chunkedList.joinToString("\n")}")
    }
}
