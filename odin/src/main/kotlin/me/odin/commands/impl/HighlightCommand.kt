package me.odin.commands.impl

import com.github.stivais.commodore.parsers.impl.GreedyString
import me.odinmain.OdinMain.onLegitVersion
import me.odinmain.commands.CommandNode
import me.odinmain.commands.Commodore
import me.odinmain.config.Config
import me.odinmain.features.impl.render.CustomESP.espList
import me.odinmain.utils.skyblock.modMessage

object HighlightCommand : Commodore {
    override val command: CommandNode =
        literal("highlight") {
            requires {
                onLegitVersion
            }

            runs {
                modMessage("Usage: /highlight <add/remove/clear/list> <name>")
            }

            literal("add").runs { mob: GreedyString ->
                val lowercase = mob.string.lowercase()
                if (lowercase in espList) return@runs modMessage("$mob is already in the Highlight list.")

                modMessage("Added $mob to the Highlight list.")
                espList.add(lowercase)
                Config.save()
            }

            literal("remove").runs { mob: GreedyString ->
                val lowercase = mob.string.lowercase()
                if (lowercase !in espList) return@runs modMessage("$mob isn't in the Highlight list.")

                modMessage("Removed $mob from the Highlight list.")
                espList.remove(lowercase)
                Config.save()
            }

            literal("clear").runs {
                modMessage("Highlight list cleared.")
                espList.clear()
                Config.save()
            }

            literal("list").runs {
                if (espList.size == 0) return@runs modMessage("Highlight list is empty")
                modMessage("Highlight list:\n${espList.joinToString("\n")}")
            }
        }
}

