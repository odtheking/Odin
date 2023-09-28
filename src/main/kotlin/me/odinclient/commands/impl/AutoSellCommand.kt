package me.odinclient.commands.impl

import me.odinclient.commands.invoke
import me.odinclient.config.MiscConfig
import me.odinclient.utils.skyblock.ChatUtils.modMessage

private inline val autoSell get() = MiscConfig.autoSell

val autoSellCommand = "autosell" {
    does {
        modMessage("Incorrect usage. Usage: add, remove, clear, list")
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
