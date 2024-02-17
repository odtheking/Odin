package me.odinclient.commands.impl

import com.github.stivais.commodore.parsers.impl.GreedyString
import me.odinclient.mixin.accessors.IMinecraftAccessor
import me.odinmain.OdinMain.mc
import me.odinmain.OdinMain.onLegitVersion
import me.odinmain.commands.CommandNode
import me.odinmain.commands.Commodore
import me.odinmain.config.MiscConfig
import me.odinmain.features.impl.render.CustomESP
import me.odinmain.utils.skyblock.modMessage

object ESPCommand : Commodore {
    override val command: CommandNode =
        literal("esp") {
            requires {
                !onLegitVersion
            }

            runs { modMessage("Usage: /esp <add/remove/clear/list> <name>")}

            literal("add").runs { mob: GreedyString ->
                val lowercase = mob.string.lowercase()
                if (lowercase in MiscConfig.espList) return@runs modMessage("$mob is already in the ESP list.")

                modMessage("Added $mob to the ESP list.")
                MiscConfig.espList.add(lowercase)
                MiscConfig.saveAllConfigs()
            }

            literal("remove").runs { mob: GreedyString ->
                val lowercase = mob.string.lowercase()
                if (lowercase !in MiscConfig.espList) return@runs modMessage("$mob isn't in the ESP list.")

                modMessage("Removed $mob from the ESP list.")
                MiscConfig.espList.remove(lowercase)
                MiscConfig.saveAllConfigs()
                CustomESP.currentEntities.clear()
            }

            literal("clear").runs {
                modMessage("ESP list cleared.")
                MiscConfig.espList.clear()
                MiscConfig.saveAllConfigs()
                CustomESP.currentEntities.clear()
            }

            literal("list").runs {
                if (MiscConfig.espList.size == 0) return@runs modMessage("ESP list is empty")
                modMessage("ESP list:\n${MiscConfig.espList.joinToString("\n")}")
            }

            literal("profile").runs { key: Int ->
                (mc as IMinecraftAccessor).invokeUpdateDebugProfilerName(key)
            }
        }
}
