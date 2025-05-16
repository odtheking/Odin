@file:Suppress("UNUSED")

package me.odinmain.commands.impl

import com.github.stivais.commodore.Commodore
import com.github.stivais.commodore.parsers.CommandParsable
import com.github.stivais.commodore.utils.GreedyString
import me.odinmain.OdinMain.display
import me.odinmain.OdinMain.mc
import me.odinmain.features.impl.render.ClickGUIModule
import me.odinmain.features.impl.render.ServerHud.colorizeFPS
import me.odinmain.features.impl.render.ServerHud.colorizePing
import me.odinmain.features.impl.render.ServerHud.colorizeTps
import me.odinmain.utils.ServerUtils
import me.odinmain.utils.fillItemFromSack
import me.odinmain.utils.skyblock.*
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.ui.clickgui.ClickGUI
import me.odinmain.utils.ui.hud.EditHUDGui
import me.odinmain.utils.writeToClipboard
import kotlin.math.round

val mainCommand = Commodore("od", "odin") {
    runs {
        display = ClickGUI
    }

    literal("edithud").runs {
        display = EditHUDGui
    }

    literal("ep").runs {
        fillItemFromSack(16, "ENDER_PEARL", "ender_pearl", true)
    }

    literal("ij").runs {
        fillItemFromSack(64, "INFLATABLE_JERRY", "inflatable_jerry", true)
    }

    literal("sl").runs {
        fillItemFromSack(16, "SPIRIT_LEAP", "spirit_leap", true)
    }

    literal("sb").runs {
        fillItemFromSack(64, "SUPERBOOM_TNT", "superboom_tnt", true)
    }

    literal("reset") {
        literal("clickgui").runs {
            ClickGUIModule.resetPositions()
            modMessage("Reset click gui positions.")
        }
        literal("hud").runs {
            EditHUDGui.resetHUDs()
            modMessage("Reset HUD positions.")
        }
    }

    literal("rq").runs {
        sendCommand("instancerequeue")
        modMessage("requeing dungeon run")
    }

    literal("help").runs {
        modMessage(
            """
             List of commands:
             §3- /od §7» §8Main command.
             §3- /od edithud §7» §8Edit HUD.
             §3- /od reset <clickgui|hud> §7» §8Resets positions accordingly. 
             §3- /dwp §7» §8Dungeon waypoints command.
             §3- /petkeys §7» §8Pet keys command.
             §3- /posmsg §7» §8Position message command.
             §3- /chatclist §7» §8Used to configure your blacklist/whitelist.
             §3- /highlight §7» §8Used to configure Highlight list.
             §3- /waypoint §7» §8Configure waypoints.
             §3- /termsim <ping>? §7» §8Simulates terminals so you can practice them.
             §3- /od rq §7» §8Requeues dungeon run.
             §3- /od m? » §8Teleports you to a floor in master mode.
             §3- /od f? » §8Teleports you to a floor in normal mode.
             §3- /od t? » §8Teleports you to a kuudra run.
             §3- /od dianareset §7» §8Resets all active diana waypoints.
             §3- /od sendcoords §7» §8Sends coords in patcher's format.
             §3- /od ping §7» §8Sends your ping in chat.
             §3- /od tps §7» §8Sends the server's tps in chat.
             §3- /od ep §7» §8Refills ender pearls up to 16.
             §3- /od ij §7» §8Refills inflatable Jerry's up to 64.
             §3- /od sl §7» §8Refills spirit leaps up to 16.
             §3- /od sb §7» §8Refills super booms up to 64.
             §3- /spcmd §7» §8Use /spcmd help for command list.
             §3- /visualwords §7» §8Command to replace words in the game.
             §3- /od leaporder §7» §8Sets custom leap order.
             """.trimIndent()
        )
    }

    literal("dianareset").runs {
        modMessage("§aResetting all active diana waypoints.")
        DianaBurrowEstimate.activeBurrows.clear()
    }

    literal("sendcoords").runs { message: GreedyString? ->
        sendChatMessage(PlayerUtils.getPositionString() + if (message == null) "" else " ${message.string}")
    }

    literal("ping").runs {
        modMessage("${colorizePing(ServerUtils.averagePing.toInt())}ms")
    }

    literal("fps").runs {
        modMessage(colorizeFPS(mc.debug.split(" ")[0].toIntOrNull() ?: 0))
    }

    literal("tps").runs {
        modMessage("${colorizeTps(round(ServerUtils.averageTps))}ms")
    }

    // separated for better error handling,
    // i.e. when a command fails it will show:
    // /od <floor> and /od <kuudra tier>
    runs { floor: Floors -> sendCommand("joininstance ${floor.instance()}") }
    runs { tier: KuudraTier -> sendCommand("joininstance ${tier.instance()}") }

    literal("leaporder").runs { player1: String?, player2: String?, player3: String?, player4: String? ->
        val players = listOf(player1, player2, player3, player4).mapNotNull { it?.lowercase() }
        DungeonUtils.customLeapOrder = players
        modMessage("§aCustom leap order set to: §f${player1}, ${player2}, ${player3}, ${player4}")
    }

    literal("copy").runs { message: GreedyString ->
        writeToClipboard(message.string, "§aCopied to clipboard.")
    }
}

@CommandParsable
private enum class Floors {
    F1, F2, F3, F4, F5, F6, F7, M1, M2, M3, M4, M5, M6, M7;

    private val floors = listOf("one", "two", "three", "four", "five", "six", "seven")
    fun instance() = "${if (ordinal > 6) "master_" else ""}catacombs_floor_${floors[(ordinal % 7)]}"
}

@CommandParsable
private enum class KuudraTier(private val test: String) {
    T1("normal"), T2("hot"), T3("burning"), T4("fiery"), T5("infernal");

    fun instance() = "kuudra_${test}"
}
