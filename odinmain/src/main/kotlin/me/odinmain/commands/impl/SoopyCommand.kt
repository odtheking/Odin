package me.odinmain.commands.impl

import com.github.stivais.commodore.parsers.impl.GreedyString
import kotlinx.coroutines.launch
import me.odinmain.OdinMain.mc
import me.odinmain.OdinMain.scope
import me.odinmain.commands.CommandNode
import me.odinmain.commands.Commodore
import me.odinmain.utils.equalsOneOf
import me.odinmain.utils.fetchURLData
import me.odinmain.utils.skyblock.modMessage

object SoopyCommand : Commodore {
    override val command: CommandNode =
        literal("spcmd") {
            runs { str: GreedyString ->
                val message = str.string.split(" ")
                var url = ""
                if (message[0].lowercase().equalsOneOf("cmds", "commands", "cmd", "command"))
                    return@runs modMessage("Commands: kuudra, auctions, skills, skillaverage, dojo, overflowskills, overflowskillaverage, bestiary, faction, nucleus, guildof, essence, secrets, bank, pet, whatdoing, dungeon, currdungeon, sblvl, classaverage, rtca, nw, ")

                when (message.size) {
                    1 -> {
                        val playerName = mc.thePlayer.name
                        val command = message[0]
                        url = "https://soopy.dev/api/soopyv2/botcommand?m=$command&u=$playerName"
                    }
                    2 -> {
                        val targetUser = message[1]
                        val command = message[0]
                        url = "https://soopy.dev/api/soopyv2/botcommand?m=$command&u=$targetUser"
                    }
                }
                modMessage("Running command...")

                scope.launch { modMessage(fetchURLData(url)) }
            }
        }


}
