package me.odinclient.commands.impl

import me.odinclient.OdinClient.Companion.miscConfig
import me.odinclient.commands.Command
import me.odinclient.commands.CommandArguments
import me.odinclient.utils.skyblock.ChatUtils.modMessage

object BlacklistCommand : Command("blacklist", listOf("odblacklist"), description = "Command for Blacklist.") {
    override fun executeCommand(args: CommandArguments) {
        if (args.isEmpty())
            modMessage("§cArguments empty. §fUsage: add, remove, clear, list")
        else {
            when (args[0]) {
                "add" -> {
                    if (args.size == 1) return modMessage("You need to name someone to add to the Blacklist. (Case Sensitive)")
                    val playerName = args.getRegular(1)
                    if (miscConfig.blacklist.contains(playerName)) return modMessage("$playerName is already in the Blacklist.")

                    modMessage("Added $playerName to Blacklist.")
                    miscConfig.blacklist.add(playerName)
                    miscConfig.saveAllConfigs()
                }

                "remove" -> {
                    if (args.size == 1) return modMessage("You need to name someone to remove from the Blacklist. (Case Sensitive)")
                    val playerName = args.getRegular(1)
                    if (!miscConfig.blacklist.contains(playerName)) return modMessage("$playerName isn't in the Blacklist.")

                    modMessage("Removed $playerName from Blacklist.")
                    miscConfig.blacklist.remove(playerName)
                    miscConfig.saveAllConfigs()
                }

                "clear" -> {
                    modMessage("Blacklist cleared.")
                    miscConfig.blacklist.clear()
                    miscConfig.saveAllConfigs()
                }

                "list" -> miscConfig.blacklist.forEach { modMessage(it) }
                else -> modMessage("Blacklist incorrect usage. Usage: add, remove, clear, list")
            }
        }
    }

    override val shortcuts: List<String> = listOf("add", "remove", "clear", "list")
}