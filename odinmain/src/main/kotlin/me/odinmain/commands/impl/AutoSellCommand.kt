package me.odinmain.commands.impl

import me.odinmain.OdinMain.Companion.miscConfig
import me.odinmain.commands.AbstractCommand
import me.odinmain.utils.skyblock.ChatUtils.modMessage

object AutoSellCommand : AbstractCommand("autosell", "odautosell", description = "Command for Auto Sell.") {

    private inline val autoSell get () = miscConfig.autoSell

    init {
        "add" {
            does {
                if (it.isEmpty()) return@does modMessage("You need to name an item.")
                val itemName = it.joinToString(" ")
                if (autoSell.contains(itemName)) return@does modMessage("$itemName is already in the Auto sell list.")

                modMessage("Added $itemName to the Auto sell list.")
                autoSell.add(itemName)
                miscConfig.saveAllConfigs()
            }
        }

        "remove" {
            does {
                if (it.size == 1) return@does modMessage("You need to name an item.")
                val itemName = it.copyOfRange(1, it.size).joinToString(" ")
                if (!autoSell.contains(itemName)) return@does modMessage("$itemName isn't in the Auto sell list.")

                modMessage("Removed $itemName from the Auto sell list.")
                autoSell.remove(itemName)
                miscConfig.saveAllConfigs()
            }
        }

        "clear" {
            does {
                modMessage("Auto sell list cleared.")
                autoSell.clear()
                miscConfig.saveAllConfigs()
            }
        }

        "list" {
            does {
                if (autoSell.isEmpty()) return@does modMessage("Auto sell list is empty!")
                autoSell.forEach { modMessage(it) }
            }
        }

        orElse {
            modMessage("what lil bro cooked")
        }
    }
}