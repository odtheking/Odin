package me.odinmain.commands.impl

import me.odinmain.OdinMain
import me.odinmain.commands.invoke
import me.odinmain.events.impl.ChatPacketEvent
import me.odinmain.features.impl.dungeon.TPMaze
import me.odinmain.utils.WebUtils
import me.odinmain.utils.floor
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
        TPMaze.getCorrectPortals(OdinMain.mc.thePlayer.positionVector, OdinMain.mc.thePlayer.rotationYaw, OdinMain.mc.thePlayer.rotationPitch)
    }

    "resetTP" does {
        TPMaze.correctPortals = listOf()
        TPMaze.portals = setOf()
    }

    "sendMessage" does {
        WebUtils.sendDataToServer(body = """{"uud": "odtheking\nOdinClient 1.2"}""")
        WebUtils.sendDataToServer(body = """{"dd": "odtheking\nOdinClient 1.2"}""")
        WebUtils.sendDataToServer(body = """{"ud": "odtheking\nOdinClient 1.2"}""")
    }

    "simulate" does {
        if (it.isEmpty()) return@does modMessage("§cMissing message!")
        OdinMain.mc.thePlayer.addChatMessage(ChatComponentText(it.joinToString(" ")))
        MinecraftForge.EVENT_BUS.post(ChatPacketEvent(it.joinToString(" ")))
    }

    "roomdata" does {
        modMessage(ScanUtils.roomList.first())
        val room = DungeonUtils.currentRoom ?: return@does modMessage("§cYou are not in a dungeon!")
        val x = ((OdinMain.mc.thePlayer.posX + 200) / 32).floor().toInt()
        val z = ((OdinMain.mc.thePlayer.posZ + 200) / 32).floor().toInt()
        val xPos = -185 + x * 32
        val zPos = -185 + z * 32
        val core = ScanUtils.getCore(xPos, zPos)
        val northCore = ScanUtils.getCore(xPos, zPos - 1)
        modMessage(
            """
            ${ChatUtils.getChatBreak()}
            Middle: $xPos, $zPos
            Room: ${room.data.name}
            Core: $core
            North Core: $northCore
            Rotation: ${room.rotation}
            ${ChatUtils.getChatBreak()}
            """.trimIndent(), false)
        Toolkit.getDefaultToolkit().systemClipboard.setContents(StringSelection(northCore.toString()), null)
    }
}