package me.odinmain.commands.impl

import com.github.stivais.commodore.utils.GreedyString
import me.odinmain.OdinMain.display
import me.odinmain.commands.commodore
import me.odinmain.features.impl.dungeon.dungeonwaypoints.DungeonWaypoints
import me.odinmain.features.impl.render.ClickGUIModule
import me.odinmain.features.impl.render.ServerHud.colorizeFPS
import me.odinmain.features.impl.render.ServerHud.colorizePing
import me.odinmain.features.impl.render.ServerHud.colorizeTps
import me.odinmain.features.impl.skyblock.DianaHelper
import me.odinmain.ui.clickgui.ClickGUI
import me.odinmain.ui.hud.EditHUDGui
import me.odinmain.utils.*
import me.odinmain.utils.skyblock.*
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import java.awt.Desktop
import java.net.URI
import kotlin.math.round

val mainCommand = commodore("od", "odin") {
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
             §3- /blacklist §7» §8Used to configure your blacklist.
             §3- /highlight §7» §8Used to configure Highlight list.
             §3- /waypoint §7» §8Configure waypoints.
             §3- /termsim {ping}? {amount}? §7» §8Simulates terminals so you can practice them.
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
             §3- /od sc <user> §7» §8Tries to open SkyCrypt for the specified user in default browser.
             §3- /spcmd §7» §8Use /spcmd cmds for command list.
             §3- /visualwords §7» §8Command to replace words in the game.
             """.trimIndent()
        )
    }

    literal("dianareset").runs {
        modMessage("Resetting all active diana waypoints.")
        DianaHelper.burrowsRender.clear()
    }

    literal("sendcoords").runs { message: GreedyString? ->
        sendChatMessage(PlayerUtils.getPositionString() + if (message == null) "" else " ${message.string}")
    }

    literal("ping").runs {
        modMessage("${colorizePing(ServerUtils.averagePing.toInt())}ms")
    }

    literal("fps").runs {
        modMessage(colorizeFPS(ServerUtils.fps))
    }

    literal("tps").runs {
        modMessage("${colorizeTps(round(ServerUtils.averageTps))}ms")
    }

    literal("dwp").runs {
        DungeonWaypoints.onKeybind()
    }

    literal("sc").runs { targetUser: String ->
        modMessage("Opening SkyCrypt for $targetUser.")
        try {
            Desktop.getDesktop().browse(URI("https://sky.shiiyu.moe/$targetUser"))
        } catch (_: Exception) {
            modMessage("Failed to open in browser.")
        }
    }

    runs { tier: String ->
        val normalizedTier = tier.trim().replace(Regex(" +"), "")
            .replace("floor", "f", ignoreCase = true)
            .replace("tier", "t", ignoreCase = true)
            .replace("master", "m", ignoreCase = true)

        if (normalizedTier[0].equalsOneOf('f', 'm')) {
            if (normalizedTier.length != 2 || normalizedTier[1] !in '1'..'7') return@runs
            sendCommand("joininstance ${if (normalizedTier[0] == 'm') "master_" else ""}catacombs_floor_${floors[normalizedTier[1]]}")
        } else if (normalizedTier[0] == 't') {
            if (normalizedTier.length != 2 || normalizedTier[1] !in '1'..'5') return@runs
            sendCommand("joininstance kuudra_${tiers[normalizedTier[1]]}")
        }
    } suggests {
        (tiers.keys.map { "t$it" } + floors.keys.map { "m$it" } + floors.keys.map { "f$it" }).toList()
    }

    literal("leap").runs { player1: String?, player2: String?, player3: String?, player4: String? ->
        val players = listOfNotNull(player1, player2, player3, player4)
        DungeonUtils.customLeapOrder = players
        modMessage("§aCustom leap order set to: §f${players.joinToString(", ")}")
    }

    literal("copy").runs { message: GreedyString ->
        writeToClipboard(message.string, "§aCopied to clipboard.")
    }
}

private val floors = mapOf(
    '1' to "one", '2' to "two", '3' to "three", '4' to "four", '5' to "five", '6' to "six", '7' to "seven"
)

private val tiers = mapOf(
    '1' to "none", '2' to "hot", '3' to "burning", '4' to "fiery", '5' to "infernal"
)
