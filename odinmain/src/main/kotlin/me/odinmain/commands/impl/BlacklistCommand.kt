package me.odinmain.commands.impl

import me.odinmain.commands.CommandNode
import me.odinmain.commands.Commodore
import me.odinmain.config.Config
import me.odinmain.features.impl.skyblock.ChatCommands.blacklist
import me.odinmain.utils.skyblock.modMessage

object BlacklistCommand : Commodore {
    override val command: CommandNode =
        literal("blacklist") {

            runs {
                modMessage("Usage: /blacklist <add/remove/clear/list> <name>")
            }

            literal("add").runs { name: String ->
                val lowercase = name.lowercase()
                if (lowercase in blacklist) return@runs modMessage("$name is already in the Blacklist.")

                modMessage("Added $name to Blacklist.")
                blacklist.add(lowercase)
                Config.save()
            }

            literal("remove").runs { name: String ->
                val lowercase = name.lowercase()
                if (lowercase !in blacklist) return@runs modMessage("$name isn't in the Blacklist.")

                modMessage("Removed $name from Blacklist.")
                blacklist.remove(lowercase)
                Config.save()
            }

            literal("clear").runs {
                modMessage("Blacklist cleared.")
                blacklist.clear()
                Config.save()
            }

            literal("list").runs {
                if (blacklist.size == 0) return@runs modMessage("Blacklist is empty")
                modMessage("Blacklist:\n${blacklist.joinToString("\n")}")
            }
        }
}
