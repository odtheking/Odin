package me.odinmain.commands.impl

import com.github.stivais.commodore.utils.GreedyString
import kotlinx.coroutines.*
import me.odinmain.OdinMain.mc
import me.odinmain.commands.commodore
import me.odinmain.events.impl.PacketReceivedEvent
import me.odinmain.features.ModuleManager.generateFeatureList
import me.odinmain.features.impl.render.DevPlayers.updateDevs
import me.odinmain.utils.*
import me.odinmain.utils.skyblock.*
import me.odinmain.utils.skyblock.dungeon.*
import net.minecraft.network.play.server.S02PacketChat
import net.minecraft.util.ChatComponentText


@OptIn(DelicateCoroutinesApi::class)
val devCommand = commodore("oddev") {

    literal("getdata") {
        literal("entity").runs { copyEntityData() }
        literal("block").runs { copyBlockData() }
    }

    literal("giveaotv").runs {
        sendCommand("give @p minecraft:diamond_shovel 1 0 {ExtraAttributes:{ethermerge:1b}}")
    }

    literal("updatedevs").runs {
       updateDevs()
    }

    literal("adddev").runs { name: String, password: String ->
        modMessage("Sending data... name: $name, password: $password")
        GlobalScope.launch {
            modMessage(sendDataToServer("$name, [1,2,3], [1,2,3], false, $password", "https://tj4yzotqjuanubvfcrfo7h5qlq0opcyk.lambda-url.eu-north-1.on.aws/"))
        }
    }

    literal("dunginfo").runs {
        modMessage("""
            ${getChatBreak()}
            |inDungeons: ${DungeonUtils.inDungeons}
            |Floor: ${DungeonUtils.floorNumber}
            |InBoss: ${DungeonUtils.inBoss}
            |Secrets: ${DungeonUtils.secretCount} / ${DungeonUtils.totalSecrets}
            |Known Secrets: ${DungeonUtils.knownSecrets}
            |Minimum Secrets: ${DungeonUtils.minSecrets}
            |Deaths: ${DungeonUtils.deathCount}
            |Crypts: ${DungeonUtils.cryptCount}
            |OpenRooms: ${DungeonUtils.openRoomCount}
            |CompletedRooms: ${DungeonUtils.completedRoomCount}
            |PercentCleared: ${DungeonUtils.percentCleared}%
            |SecretsRemaining: ${DungeonUtils.secretsRemaining}
            |TotalRooms: ${DungeonUtils.totalRooms}
            |DungeonTime: ${DungeonUtils.dungeonTime}
            |isGhost: ${DungeonUtils.isGhost}
            |currentDungeonPlayer: ${DungeonUtils.currentDungeonPlayer.name}, ${DungeonUtils.currentDungeonPlayer.clazz}, ${DungeonUtils.currentDungeonPlayer.isDead}
            |mimicKilled: ${DungeonUtils.mimicKilled}
            |doorOpener: ${DungeonUtils.doorOpener}
            |passedRooms: ${DungeonUtils.passedRooms.map { it.room.data.name }}
            |currentRoom: ${DungeonUtils.currentRoom?.room?.data?.name}
            |Teammates: ${DungeonUtils.dungeonTeammates.joinToString { "${it.name} (${it.clazz})" }}
            |TeammatesNoSelf: ${DungeonUtils.dungeonTeammatesNoSelf.map { it.name }}
            |LeapTeammates: ${DungeonUtils.leapTeammates.map { it.name }}
            |Blessings: ${Blessings.entries.joinToString { "${it.name}: ${it.current}" }}
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
        val room = DungeonUtils.currentRoom //?: return@does modMessage("§cYou are not in a dungeon!")
        val x = ((mc.thePlayer.posX + 200) / 32).toInt()
        val z = ((mc.thePlayer.posZ + 200) / 32).toInt()
        val xPos = -185 + x * 32
        val zPos = -185 + z * 32
        val core = ScanUtils.getCore(xPos, zPos)
        modMessage(
            """
                    ${getChatBreak()}
                    Middle: $xPos, $zPos
                    Room: ${room?.room?.data?.name}
                    Core: $core
                    Rotation: ${room?.room?.rotation}
                    Positions: ${room?.positions}
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
