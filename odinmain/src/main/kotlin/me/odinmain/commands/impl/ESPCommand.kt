package me.odinmain.commands.impl

import me.odinmain.OdinMain
import me.odinmain.commands.invoke
import me.odinmain.config.MiscConfig
import me.odinmain.features.impl.render.CustomESP
import me.odinmain.utils.skyblock.ChatUtils.modMessage

private inline val espList get() = MiscConfig.espList
private val name = if (OdinMain.onLegitVersion) "highlight" else "ESP"

val espCommand = name {
    does {
        modMessage("$name incorrect usage. Usage: add, remove, clear, list")
    }

    "add" does {
        if (it.isEmpty()) return@does modMessage("§cMissing mob name!")
        val mobName = it.joinToString(" ")
        if (mobName in espList) return@does modMessage("$mobName is already on the $name list.")

        modMessage("Added $mobName to the $name list.")
        espList.add(mobName)
        MiscConfig.saveAllConfigs()
    }

    "remove" does {
        if (it.isEmpty()) return@does modMessage("§cMissing mob $name!")
        val mobName = it.joinToString(" ")
        if (mobName !in espList) return@does modMessage("$mobName isn't on the list.")

        modMessage("Removed $mobName from the $name list.")
        espList.remove(mobName)
        MiscConfig.saveAllConfigs()
        CustomESP.currentEntities.clear()
    }


    "clear" does {
        espList.clear()
        MiscConfig.saveAllConfigs()
        CustomESP.currentEntities.clear()
        modMessage("$name List cleared.")
    }


    "list" does {
        espList.forEach { modMessage(it) }
    }

    "help" does {
        modMessage("Usage: add, remove, clear, list")
    }
}