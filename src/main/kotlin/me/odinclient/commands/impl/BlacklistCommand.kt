package me.odinclient.commands.impl

import me.odinclient.OdinClient.Companion.miscConfig
import me.odinclient.commands.AbstractCommand
import me.odinclient.utils.skyblock.ChatUtils.modMessage

object BlacklistCommand : AbstractCommand("blacklist", "odblacklist", description = "Command for Blacklist.") {
    init {
        empty {
            modMessage("§cArguments empty. §fUsage: add, remove, clear, list")
        }

        "add" {
            does {
                if (it.isEmpty()) return@does modMessage("You need to name someone to add to the Blacklist.")
                val name = it[0]
                if (name !in miscConfig.blacklist) return@does modMessage("$name is already in the Blacklist.")

                modMessage("Added $name to Blacklist.")
                miscConfig.blacklist.add(name)
                miscConfig.saveAllConfigs()
            }
        }

        "remove" {
            does {
                if (it.isEmpty()) return@does modMessage("You need to name someone to remove from the Blacklist.")
                val name = it[0]
                if (name !in miscConfig.blacklist) return@does modMessage("$name isn't in the Blacklist.")

                modMessage("Removed $name from Blacklist.")
                miscConfig.blacklist.remove(name)
                miscConfig.saveAllConfigs()
            }
        }

        "clear" {
            does {
                modMessage("Blacklist cleared.")
                miscConfig.blacklist.clear()
                miscConfig.saveAllConfigs()
            }
        }

        "list" {
            does {
                miscConfig.blacklist.forEach { modMessage(it) }
            }
        }

        orElse {
            modMessage("Blacklist incorrect usage. Usage: add, remove, clear, list")
        }
    }
}