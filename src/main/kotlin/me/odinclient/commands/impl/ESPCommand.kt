package me.odinclient.commands.impl

import me.odinclient.OdinClient.Companion.mc
import me.odinclient.commands.Command
import me.odinclient.commands.CommandArguments
import me.odinclient.config.MiscConfig
import me.odinclient.features.impl.render.ESP
import me.odinclient.utils.skyblock.ChatUtils.modMessage

object ESPCommand : Command("esp", listOf("odesp"), "Command for ESP.") {

    private inline val espList get() = MiscConfig.espList

    override fun executeCommand(args: CommandArguments) {
        if (args.isEmpty())
            modMessage("§cArguments empty. §fUsage: add, remove, clear, list")
        else {
            when (args[0]) {
                "add" -> {
                    if (args.size == 1) return modMessage("§cMissing mob name!")
                    val mobName = args.joinToString(1)
                    if (mobName in espList) return modMessage("$mobName is already on the ESP list.")

                    modMessage("Added $mobName to the ESP list.")
                    espList.add(mobName)
                    MiscConfig.saveAllConfigs()
                }

                "remove" -> {
                    if (args.size == 1) return modMessage("§cMissing mob name!")
                    val mobName = args.joinToString(1)
                    if (mobName !in espList) return modMessage("$mobName isn't on the list.")

                    modMessage("Removed $mobName from the ESP list.")
                    espList.remove(mobName)
                    MiscConfig.saveAllConfigs()
                    ESP.currentEntities.clear()
                }

                "clear" -> {
                    espList.clear()
                    MiscConfig.saveAllConfigs()
                    ESP.currentEntities.clear()
                    modMessage("ESP List cleared.")
                }

                "list" -> espList.forEach { modMessage(it) }
                "help" -> modMessage("Usage: add, remove, clear, list")
                else -> modMessage("§cIncorrect Usage. §rUsage: add, remove, clear, list")
            }
        }
    }

    override val shortcuts: List<String> = listOf("add", "remove", "clear", "list")
}
