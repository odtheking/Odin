package me.odinmain.commands.impl

import com.github.stivais.commodore.Commodore
import com.github.stivais.commodore.utils.SyntaxException
import kotlinx.coroutines.launch
import me.odinmain.OdinMain.mc
import me.odinmain.OdinMain.scope
import me.odinmain.utils.network.WebUtils.streamAndRead
import me.odinmain.utils.skyblock.modMessage

val soopyCommand = Commodore("soopycmd", "spcmd", "spc") {
    val commands = listOf(
        "auctions", "bestiary", "bank", "classaverage", "currdungeon", "dojo", "dungeon", "essence", "faction",
        "guildof", "kuudra", "nucleus", "nw", "overflowskillaverage", "overflowskills", "pet", "rtca", "sblvl",
        "secrets", "skillaverage", "skills"
    )
    literal("help").runs {
        modMessage("Available commands for /spcmd:\n ${commands.joinToString()}")
    }

    executable {
        param("command") {
            parser { string: String ->
                if (!commands.contains(string)) throw SyntaxException("Invalid argument.")
                string
            }
            suggests { commands }
        }

        runs { command: String, user: String? ->
            val player = user ?: mc.thePlayer.name
            modMessage("Running command...")
            scope.launch {
                streamAndRead<String>("https://soopy.dev/api/soopyv2/botcommand?m=$command&u=$player").fold(
                    { modMessage(it) },
                    { e -> modMessage("Failed to fetch data: ${e.message}") }
                )
            }
        }
    }
}
