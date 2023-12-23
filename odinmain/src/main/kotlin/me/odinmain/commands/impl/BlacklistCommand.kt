package me.odinmain.commands.impl

import me.odinmain.commands.invoke
import me.odinmain.config.MiscConfig
import me.odinmain.utils.skyblock.modMessage

val blacklistCommand = "blacklist" {
    does {
        modMessage("§cBlacklist incorrect usage. §fUsage: add, remove, clear, list")
    }

    "add" does {
        if (it.isEmpty()) return@does modMessage("You need to name someone to add to the Blacklist.")
        val name = it[0]
        if (name in MiscConfig.blacklist) return@does modMessage("$name is already in the Blacklist.")

        modMessage("Added $name to Blacklist.")
        MiscConfig.blacklist.add(name.lowercase())
        MiscConfig.saveAllConfigs()
    }

    "remove" does {
        if (it.isEmpty()) return@does modMessage("You need to name someone to remove from the Blacklist.")
        val name = it[0]
        if (name !in MiscConfig.blacklist) return@does modMessage("$name isn't in the Blacklist.")

        modMessage("Removed $name from Blacklist.")
        MiscConfig.blacklist.remove(name.lowercase())
        MiscConfig.saveAllConfigs()
    }


    "clear" does {
        modMessage("Blacklist cleared.")
        MiscConfig.blacklist.clear()
        MiscConfig.saveAllConfigs()
    }


    "list" does {
        MiscConfig.blacklist.forEach { modMessage(it) }
    }
}
