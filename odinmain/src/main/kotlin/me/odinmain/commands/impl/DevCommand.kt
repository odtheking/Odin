package me.odinmain.commands.impl

import me.odinmain.OdinMain
import me.odinmain.OdinMain.mc
import me.odinmain.commands.invoke
import me.odinmain.events.impl.ChatPacketEvent
import me.odinmain.features.impl.dungeon.TPMaze
import me.odinmain.utils.WebUtils
import me.odinmain.utils.skyblock.ChatUtils
import me.odinmain.utils.skyblock.ChatUtils.modMessage
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.skyblock.dungeon.ScanUtils
import net.minecraft.util.ChatComponentText
import net.minecraftforge.common.MinecraftForge
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

val devCommand = "oddev" {
    "testTP" does {
        TPMaze.getCorrectPortals(mc.thePlayer.positionVector, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch)
    }

    "resetTP" does {
        TPMaze.correctPortals = listOf()
        TPMaze.portals = setOf()
    }

    "giveaotv" does {
        ChatUtils.sendCommand("give @p minecraft:diamond_shovel 1 0 {ExtraAttributes:{ethermerge:1b}}")
    }

    "sendMessage" does {
        WebUtils.sendDataToServer(body = """{"ud": "${mc.thePlayer.name}\n${ if (OdinMain.onLegitVersion) "legit" else "cheater"} ${OdinMain.VERSION}"}""")
        WebUtils.sendDataToServer(body = """{"dd": "odtheking\nOdinClient 1.2"}""")
        WebUtils.sendDataToServer(body = """{"ud": "${mc.thePlayer.name}\n${ if (OdinMain.onLegitVersion) "legit" else "cheater"} ${OdinMain.VERSION}"}""")
    }

    "simulate" does {
        if (it.isEmpty()) return@does modMessage("§cMissing message!")
        mc.thePlayer.addChatMessage(ChatComponentText(it.joinToString(" ")))
        MinecraftForge.EVENT_BUS.post(ChatPacketEvent(it.joinToString(" ")))
    }

    "roomdata" does {
        val room = DungeonUtils.currentRoom //?: return@does modMessage("§cYou are not in a dungeon!")
        val x = ((mc.thePlayer.posX + 200) / 32).toInt()
        val z = ((mc.thePlayer.posZ + 200) / 32).toInt()
        val xPos = -185 + x * 32
        val zPos = -185 + z * 32
        val core = ScanUtils.getCore(xPos, zPos)
        val northCore = ScanUtils.getCore(xPos, zPos - 1)
        modMessage(
            """
            ${ChatUtils.getChatBreak()}
            Middle: $xPos, $zPos
            Room: ${room?.data?.name}
            Core: $core
            North Core: $northCore
            Rotation: ${room?.rotation}
            ${ChatUtils.getChatBreak()}
            """.trimIndent(), false)
        Toolkit.getDefaultToolkit().systemClipboard.setContents(StringSelection(northCore.toString()), null)
    }
}