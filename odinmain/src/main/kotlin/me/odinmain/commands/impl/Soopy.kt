package me.odinmain.commands.impl

import com.github.stivais.commodore.parsers.impl.GreedyString
import kotlinx.coroutines.launch
import me.odinmain.OdinMain.display
import me.odinmain.OdinMain.mc
import me.odinmain.OdinMain.scope
import me.odinmain.commands.CommandNode
import me.odinmain.commands.Commodore
import me.odinmain.utils.fetchURLData
import me.odinmain.utils.skyblock.modMessage

object Soopy : Commodore {


    override val command: CommandNode =
        literal("soopy") {
            runs { str: GreedyString ->
                val message = str.string.split(" ")
                var output = ""
                scope.launch {
                    when (message.size) {
                        1 -> output = fetchURLData("https://soopy.dev/api/guildBot/runCommand?user=${mc.thePlayer.name}&cmd=${message[0]}")
                        2 -> output = fetchURLData("https://soopy.dev/api/guildBot/runCommand?user=${message[1]}&cmd=${message[0]}")
                        else -> modMessage("Invalid command.")
                    }
                    if (!output.substringAfter("success\":").substringBefore(",").toBoolean())
                        modMessage("Failed $output")

                    modMessage(output.substringAfter("raw\":\"").substringBefore("\""))
                }
            }

        }


}
