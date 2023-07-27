package me.odinclient.commands.impl

import me.odinclient.commands.AbstractCommand
import me.odinclient.utils.skyblock.ChatUtils.modMessage

object AutoSellCommand : AbstractCommand(
    name = "autosell",
    alias = arrayListOf("odautosell"),
    description = "Command for Auto Sell."
) {

    init {

        empty {
            modMessage("Actually empty")
        }

        "1" - {
            "2" - {
                does {
                    modMessage("2")
                }

                "3" does {
                    modMessage("3")
                }
            }

            "holy" - {
                does {
                    modMessage("holy")
                }

                "moly" does {
                    modMessage("moly")
                }
            }
        }
    }


    /*
    private inline val autoSell get () = miscConfig.autoSell
    override val errorMsg: String get() = "Incorrect usage. Usage: ${subcommands.keys.joinToString(", ") }"

    init {
        "add" does {
            if (it.size == 1) throw Throwable("You need to name an item.")
            val itemName = it.joinToString(startIndex = 1)
            if (autoSell.contains(itemName)) throw Throwable("$itemName is already in the Auto sell list.")

            modMessage("Added $itemName to the Auto sell list.")
            autoSell.add(itemName)
            miscConfig.saveAllConfigs()
        }

        "remove" does {
            if (it.size == 1) throw Throwable("You need to name an item.")
            val itemName = it.joinToString(1)
            if (!autoSell.contains(itemName)) throw Throwable("$itemName isn't in the Auto sell list.")

            modMessage("Removed $itemName from the Auto sell list.")
            autoSell.remove(itemName)
            miscConfig.saveAllConfigs()
        }

        "clear" does {
            modMessage("Auto sell list cleared.")
            autoSell.clear()
            miscConfig.saveAllConfigs()
        }

        "list" does {
            autoSell.forEach { modMessage(it) }
        }
    }

     */
}