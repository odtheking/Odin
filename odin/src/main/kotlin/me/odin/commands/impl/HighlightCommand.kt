package me.odin.commands.impl

import com.github.stivais.commodore.parsers.impl.GreedyString
import me.odinmain.OdinMain.onLegitVersion
import me.odinmain.commands.CommandNode
import me.odinmain.commands.Commodore
import me.odinmain.config.MiscConfig
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
                if (lowercase in MiscConfig.espList) return@runs modMessage("$mob is already in the Highlight list.")

                modMessage("Added $mob to the Highlight list.")
                MiscConfig.espList.add(lowercase)
                MiscConfig.saveAllConfigs()
            }

            literal("remove").runs { mob: GreedyString ->
                val lowercase = mob.string.lowercase()
                if (lowercase !in MiscConfig.espList) return@runs modMessage("$mob isn't in the Highlight list.")

                modMessage("Removed $mob from the Highlight list.")
                MiscConfig.espList.remove(lowercase)
                MiscConfig.saveAllConfigs()
            }

            literal("clear").runs {
                modMessage("Highlight list cleared.")
                MiscConfig.espList.clear()
                MiscConfig.saveAllConfigs()
            }

            literal("list").runs {
                if (MiscConfig.espList.size == 0) return@runs modMessage("Highlight list is empty")
                modMessage("Highlight list:\n${MiscConfig.espList.joinToString("\n")}")
            }
        }
}

