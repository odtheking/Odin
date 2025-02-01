package me.odinmain.commands.impl

import com.github.stivais.commodore.Commodore
import me.odinmain.config.Config
import me.odinmain.features.impl.skyblock.ChatCommands.blacklist
import me.odinmain.utils.skyblock.modMessage

val chatCommandsCommand = Commodore("chatcommandslist", "cclist", "chatclist", "ccommandslist") {
    literal("add").runs { name: String ->
        val lowercase = name.lowercase()
        if (lowercase in blacklist) return@runs modMessage("$name is already in the list.")
        modMessage("Added $name to list.")
        blacklist.add(lowercase)
        Config.save()
    }

    literal("remove").runs { name: String ->
        val lowercase = name.lowercase()
        if (lowercase !in blacklist) return@runs modMessage("$name isn't in the list.")
        modMessage("Removed $name from list.")
        blacklist.remove(lowercase)
        Config.save()
    }

    literal("clear").runs {
        modMessage("List cleared.")
        blacklist.clear()
        Config.save()
    }

    literal("list").runs {
        if (blacklist.isEmpty()) return@runs modMessage("List is empty.")
        modMessage("List:\n${blacklist.joinToString("\n")}")
    }
}
