package me.odinmain.commands.impl

import me.odinmain.commands.invoke
import me.odinmain.config.MiscConfig
import me.odinmain.utils.skyblock.modMessage

private inline val autoSell get() = MiscConfig.autoSell

val autoSellCommand = "autosell" {
    does {
        modMessage("§cIncorrect usage. §fUsage: add, remove, clear, list")
    }

    "add" does {
        if (it.isEmpty()) return@does modMessage("You need to name an item.")
        val itemName = it.joinToString(" ")
        if (itemName in autoSell) return@does modMessage("$itemName is already in the Auto sell list.")

        modMessage("Added $itemName to the Auto sell list.")
        autoSell.add(itemName)
        MiscConfig.saveAllConfigs()
    }

    "remove" does {
        if (it.size == 1) return@does modMessage("You need to name an item.")
        val itemName = it.copyOfRange(1, it.size).joinToString(" ")
        if (itemName !in autoSell) return@does modMessage("$itemName isn't in the Auto sell list.")

        modMessage("Removed $itemName from the Auto sell list.")
        autoSell.remove(itemName)
        MiscConfig.saveAllConfigs()
    }

    "clear" does {
        modMessage("Auto sell list cleared.")
        autoSell.clear()
        MiscConfig.saveAllConfigs()
    }

    "list" does {
        if (autoSell.isEmpty()) return@does modMessage("Auto sell list is empty!")
        autoSell.forEach { modMessage(it) }
    }
}