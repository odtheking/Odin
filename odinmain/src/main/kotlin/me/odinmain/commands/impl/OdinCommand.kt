package me.odinmain.commands.impl

import com.github.stivais.commodore.utils.SyntaxException
import me.odinmain.OdinMain.display
import me.odinmain.OdinMain.mc
import me.odinmain.commands.commodore
import me.odinmain.features.impl.dungeon.DungeonWaypoints
import me.odinmain.features.impl.render.ClickGUIModule
import me.odinmain.features.impl.render.ServerDisplay.colorizePing
import me.odinmain.features.impl.render.ServerDisplay.colorizeTps
import me.odinmain.features.impl.skyblock.DianaHelper
import me.odinmain.ui.clickgui.ClickGUI
import me.odinmain.ui.hud.EditHUDGui
import me.odinmain.utils.ServerUtils
import me.odinmain.utils.equalsOneOf
import me.odinmain.utils.skyblock.*
import me.odinmain.utils.skyblock.PlayerUtils.posX
import me.odinmain.utils.skyblock.PlayerUtils.posY
import me.odinmain.utils.skyblock.PlayerUtils.posZ
import kotlin.math.round

val mainCommand = commodore("od", "odin", "odinclient") {
    runs {
        display = ClickGUI
    }

    literal("ep").runs {
        val pearls = mc.thePlayer.inventory.mainInventory.find { it?.itemID == "ENDER_PEARL" }?.stackSize ?: 0
        sendCommand("gfs ender_pearl ${16 - pearls}")
    }

    literal("ij").runs {
        val jerries = mc.thePlayer.inventory.mainInventory.find { it?.itemID == "INFLATABLE_JERRY" }?.stackSize ?: 0
        sendCommand("gfs inflatable_jerry ${64 - jerries}")
    }

    literal("sl").runs {
        val leaps = mc.thePlayer.inventory.mainInventory.find { it?.itemID == "SPIRIT_LEAP" }?.stackSize ?: 0
        sendCommand("gfs spirit_leap ${16 - leaps}")
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
             §3- /spcmd §7» §8Use /spcmd cmds for command list.
             §3- /visualwords §7» §8Command to replace words in the game.
             """.trimIndent()
        )
    }

    literal("dianareset").runs {
        modMessage("Resetting all active diana waypoints.")
        DianaHelper.burrowsRender.clear()
    }

    literal("sendcoords").runs {
        sendChatMessage("x: ${posX.toInt()}, y: ${posY.toInt()}, z: ${posZ.toInt()}")
    }

    literal("ping").runs {
        modMessage("${colorizePing(ServerUtils.averagePing.toInt())}ms")
    }

    literal("tps").runs {
        modMessage("${colorizeTps(round(ServerUtils.averageTps))}ms")
    }

    literal("dwp").runs {
        DungeonWaypoints.onKeybind()
    }

    runs { tier: String ->
        if(tier[0].equalsOneOf('f', 'm')) {
            if (tier.length != 2 || tier[1] !in '1'..'7') throw SyntaxException()
            sendCommand("joininstance ${if (tier[0] == 'm') "master_" else ""}catacombs_floor_${floors[tier[1]]}")
        }
        else if (!tier[0].equals('t')){
            if (tier.length != 2 || tier[1] !in '1'..'5') throw SyntaxException()
            sendCommand("joininstance kuudra_${tiers[tier[1]]}")
        }
    } suggests {
        (tiers.keys.map { "t$it" } + floors.keys.map { "m$it" } + floors.keys.map { "f$it" }).toList()
    }
}

private val floors = mapOf(
    '1' to "one", '2' to "two", '3' to "three", '4' to "four", '5' to "five", '6' to "six", '7' to "seven"
)

private val tiers = mapOf(
    '1' to "basic", '2' to "hot", '3' to "burning", '4' to "fiery", '5' to "infernal"
)
