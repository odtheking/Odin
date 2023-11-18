package me.odinmain.commands.impl

import me.odinmain.OdinMain
import me.odinmain.OdinMain.mc
import me.odinmain.commands.invoke
import me.odinmain.events.impl.ChatPacketEvent
import me.odinmain.features.impl.dungeon.TPMaze
import me.odinmain.utils.DevUtils
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

    "getdata" does {
        if (it[0] == "entity") DevUtils.copyEntityData()
        if (it[0] == "block") DevUtils.copyBlockData()
    }

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
        val northPos = DungeonUtils.Vec2(xPos, zPos - 4)
        val northCore = ScanUtils.getCore(northPos.x, northPos.z)
        modMessage(
            """
            ${ChatUtils.getChatBreak()}
            Middle: $xPos, $zPos
            Room: ${room?.room?.data?.name}
            Core: $core
            North Core: $northCore
            North Pos: ${northPos.x}, ${northPos.z}
            Rotation: ${room?.room?.rotation}
            Positions: ${room?.positions}
            ${ChatUtils.getChatBreak()}
            """.trimIndent(), false)
        Toolkit.getDefaultToolkit().systemClipboard.setContents(StringSelection(northCore.toString()), null)
    }

    "getCore" does {
        val core = ScanUtils.getCore(mc.thePlayer.posX.toInt(), mc.thePlayer.posZ.toInt())
        modMessage("Core: $core")
        Toolkit.getDefaultToolkit().systemClipboard.setContents(StringSelection(core.toString()), null)
    }
}