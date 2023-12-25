package me.odinmain.commands.impl

import me.odinmain.commands.CommandNode
import me.odinmain.commands.Commodore
import me.odinmain.config.MiscConfig
import me.odinmain.utils.skyblock.modMessage

object BlacklistCommand : Commodore {
    override val command: CommandNode =
        literal("blacklist") {

            literal("add").runs { name: String ->
                val lowercase = name.lowercase()
                if (lowercase in MiscConfig.blacklist) return@runs modMessage("$name is already in the Blacklist.")

                modMessage("Added $name to Blacklist.")
                MiscConfig.blacklist.add(lowercase)
                MiscConfig.saveAllConfigs()
            }

            literal("remove").runs { name: String ->
                val lowercase = name.lowercase()
                if (lowercase !in MiscConfig.blacklist) return@runs modMessage("$name isn't in the Blacklist.")

                modMessage("Removed $name from Blacklist.")
                MiscConfig.blacklist.remove(lowercase)
                MiscConfig.saveAllConfigs()
            }

            literal("clear").runs {
                modMessage("Blacklist cleared.")
                MiscConfig.blacklist.clear()
                MiscConfig.saveAllConfigs()
            }

            literal("list").runs {
                if (MiscConfig.blacklist.size == 0) return@runs modMessage("Blacklist is empty")
                modMessage("Blacklist:\n${MiscConfig.blacklist.joinToString("\n")}")
            }
        }
}
