package me.odinmain.commands.impl

import com.github.stivais.commodore.utils.GreedyString
import kotlinx.coroutines.launch
import me.odinmain.OdinMain.mc
import me.odinmain.OdinMain.scope
import me.odinmain.commands.commodore
import me.odinmain.events.impl.PacketReceivedEvent
import me.odinmain.features.ModuleManager.generateFeatureList
import me.odinmain.features.impl.dungeon.MapInfo
import me.odinmain.features.impl.render.DevPlayers.updateDevs
import me.odinmain.utils.*
import me.odinmain.utils.skyblock.*
import me.odinmain.utils.skyblock.dungeon.*
import me.odinmain.utils.skyblock.dungeon.ScanUtils.getRoomCenter
import net.minecraft.network.play.server.S02PacketChat
import net.minecraft.util.ChatComponentText


val devCommand = commodore("oddev") {

    literal("giveaotv").runs {
        sendCommand("give @p minecraft:diamond_shovel 1 0 {ExtraAttributes:{ethermerge:1b}}")
    }

    literal("updatedevs").runs {
       updateDevs()
    }

    literal("adddev").runs { name: String, password: String ->
        modMessage("Sending data... name: $name, password: $password")
        scope.launch {
            modMessage(sendDataToServer("$name, [1,2,3], [1,2,3], true, $password", "https://tj4yzotqjuanubvfcrfo7h5qlq0opcyk.lambda-url.eu-north-1.on.aws/"))
        }
    }

    literal("dunginfo").runs {
        modMessage("""
            ${getChatBreak()}
            |inDungeons: ${DungeonUtils.inDungeons}
            |InBoss: ${DungeonUtils.inBoss}
            |Floor: ${DungeonUtils.floor.name}
            |Score: ${DungeonUtils.score}${when (MapInfo.togglePaul) {1 -> ", Force disabled Paul"; 2 -> ", Force enabled Paul"; else -> "" }}
            |Secrets: (${DungeonUtils.secretCount} - ${DungeonUtils.neededSecretsAmount} - ${DungeonUtils.totalSecrets} - ${DungeonUtils.knownSecrets}) 
            |mimicKilled: ${DungeonUtils.mimicKilled}
            |Deaths: ${DungeonUtils.deathCount}, Crypts: ${DungeonUtils.cryptCount}
            |BonusScore: ${DungeonUtils.getBonusScore}, isPaul: ${DungeonUtils.isPaul}
            |OpenRooms: ${DungeonUtils.openRoomCount}, CompletedRooms: ${DungeonUtils.completedRoomCount} ${DungeonUtils.percentCleared}%, Blood Done: ${DungeonUtils.bloodDone}, Total: ${DungeonUtils.totalRooms}
            |Puzzles: ${DungeonUtils.puzzles.joinToString { "${it.name} (${it.status.toString()})" }}, Count: ${DungeonUtils.puzzleCount}
            |DungeonTime: ${DungeonUtils.dungeonTime}
            |currentDungeonPlayer: ${DungeonUtils.currentDungeonPlayer.name}, ${DungeonUtils.currentDungeonPlayer.clazz}, ${DungeonUtils.currentDungeonPlayer.isDead}, ${DungeonUtils.isGhost}
            |doorOpener: ${DungeonUtils.doorOpener}
            |currentRoom: ${DungeonUtils.currentFullRoom?.room?.data?.name}, roomsPassed: ${DungeonUtils.passedRooms.map { it.room.data.name }}
            |Teammates: ${DungeonUtils.dungeonTeammates.joinToString { "${it.name} (${it.clazz})" }}
            |TeammatesNoSelf: ${DungeonUtils.dungeonTeammatesNoSelf.map { it.name }}
            |LeapTeammates: ${DungeonUtils.leapTeammates.map { it.name }}
            |Blessings: ${Blessing.entries.joinToString { "${it.name}: ${it.current}" }}
            ${getChatBreak()}
        """.trimIndent(), false)
    }

    literal("getlocation").runs {
        modMessage("currentarea: ${LocationUtils.currentArea}, isDungeon ${DungeonUtils.inDungeons}, inKuudra: ${KuudraUtils.inKuudra} kuudratier: ${LocationUtils.kuudraTier}, dungeonfloor: ${DungeonUtils.floorNumber}")
    }

    literal("simulate").runs { str: GreedyString ->
        mc.thePlayer.addChatMessage(ChatComponentText(str.string))
        PacketReceivedEvent(S02PacketChat(ChatComponentText(str.string))).postAndCatch()
    }

	literal("roomdata").runs {
        val room = DungeonUtils.currentFullRoom ?: return@runs modMessage("§cYou are not in a dungeon!")
        val roomCenter = getRoomCenter(mc.thePlayer.posX.toInt(), mc.thePlayer.posZ.toInt())
        val core = ScanUtils.getCore(roomCenter)
        modMessage(
            """
            ${getChatBreak()}
            Middle: ${roomCenter.x}, ${roomCenter.z}
            Room: ${room.room.data.name}
            Core: $core
            Rotation: ${room.room.rotation}
            Positions: ${room.extraRooms.joinToString { "(${it.x}, ${it.z})" }}
            ${getChatBreak()}
            """.trimIndent(), false
        )
        writeToClipboard(core.toString(), "Copied $core to clipboard!")
    }

    literal("generatereadme").runs {
        val readmeContent = generateFeatureList()

        writeToClipboard(readmeContent)
    }
}
