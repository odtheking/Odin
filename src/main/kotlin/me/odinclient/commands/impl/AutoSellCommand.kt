package me.odinclient.commands.impl

import me.odinclient.OdinClient.Companion.miscConfig
import me.odinclient.commands.Command
import me.odinclient.utils.skyblock.ChatUtils.modMessage

object AutoSellCommand : Command("autosell", listOf("odautosell")) {
    override fun executeCommand(args: Array<String>) {
        if (args.isEmpty())
            modMessage("Auto sell incorrect usage. Usage: add, remove, clear, list")
        else {
            when (args[0]) {
                "add" -> {
                    if (args.size == 1) return modMessage("You need to name an item.")
                    val itemName = args.joinToString(1)
                    if (miscConfig.autoSell.contains(itemName)) return modMessage("$itemName is already in the Auto sell list.")

                    modMessage("Added $itemName to the Auto sell list.")
                    miscConfig.autoSell.add(itemName)
                    miscConfig.saveAllConfigs()
                }

                "remove" -> {
                    if (args.size == 1) return modMessage("You need to name an item.")
                    val itemName = args.joinToString(1)
                    if (!miscConfig.autoSell.contains(itemName)) return modMessage("$itemName isn't in the Auto sell list.")

                    modMessage("Removed $itemName from the Auto sell list.")
                    miscConfig.autoSell.remove(itemName)
                    miscConfig.saveAllConfigs()
                }

                "clear" -> {
                    modMessage("Auto sell list cleared.")
                    miscConfig.autoSell.clear()
                    miscConfig.saveAllConfigs()
                }

                "list" -> miscConfig.autoSell.forEach { modMessage(it) }
                else -> modMessage("Incorrect usage. Usage: add, remove, clear, list")
            }
        }
    }
}