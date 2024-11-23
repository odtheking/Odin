package me.odinmain.commands.impl

import com.github.stivais.commodore.utils.GreedyString
import kotlinx.coroutines.launch
import me.odinmain.OdinMain.mc
import me.odinmain.OdinMain.scope
import me.odinmain.commands.commodore
import me.odinmain.events.impl.PacketEvent
import me.odinmain.features.ModuleManager.generateFeatureList
import me.odinmain.features.impl.dungeon.MapInfo
import me.odinmain.features.impl.floor7.DragonPriority.displaySpawningDragon
import me.odinmain.features.impl.floor7.DragonPriority.findPriority
import me.odinmain.features.impl.floor7.WitherDragonState
import me.odinmain.features.impl.floor7.WitherDragons.priorityDragon
import me.odinmain.features.impl.floor7.WitherDragonsEnum
import me.odinmain.features.impl.nether.NoPre
import me.odinmain.features.impl.render.DevPlayers.updateDevs
import me.odinmain.utils.*
import me.odinmain.utils.skyblock.*
import me.odinmain.utils.skyblock.dungeon.*
import me.odinmain.utils.skyblock.dungeon.DungeonUtils.getRelativeCoords
import me.odinmain.utils.skyblock.dungeon.ScanUtils.getRoomCenter
import net.minecraft.network.play.server.S02PacketChat
import net.minecraft.util.ChatComponentText


val devCommand = commodore("oddev") {

    literal("drags") {
        runs { text: GreedyString ->
            WitherDragonsEnum.entries.forEach {
                if (text.string.lowercase().contains(it.name.lowercase())) {
                    when (it.name) {
                        "Red" -> sendChatMessage("/particle flame 27 18 60 1 1 1 1 100 force")
                        "Orange" -> sendChatMessage("/particle flame 84 18 56 1 1 1 1 100 force")
                        "Green" -> sendChatMessage("/particle flame 26 18 95 1 1 1 1 100 force")
                        "Blue" -> sendChatMessage("/particle flame 84 18 95 1 1 1 1 100 force")
                        "Purple" -> sendChatMessage("/particle flame 57 18 125 1 1 1 1 100 force")
                    }
                }
            }
        }

        literal("reset").runs { soft: Boolean? ->
            WitherDragonsEnum.reset(soft == true)
        }

        literal("status").runs {
            val (spawned, dragons) = WitherDragonsEnum.entries.fold(0 to mutableListOf<WitherDragonsEnum>()) { (spawned, dragons), dragon ->
                val newSpawned = spawned + dragon.timesSpawned

                if (dragon.state == WitherDragonState.SPAWNING) {
                    if (dragon !in dragons) {
                        modMessage("§${dragon.colorCode}${dragon.name} has been added to spawning list!")
                        dragons.add(dragon)
                    } else modMessage("§${dragon.colorCode}${dragon.name} his already in spawning list!!")
                    return@fold newSpawned to dragons
                }

                modMessage("§${dragons.size} is the current size of dragons")
                modMessage("§${newSpawned} is how many dragons have spawned in total")
                modMessage("§${dragon.colorCode}${dragon.name} has been skipped, since it isnt spawning!!")

                newSpawned to dragons
            }

            if (dragons.size == 2 || spawned > 2) {
                priorityDragon = findPriority(dragons)
                displaySpawningDragon(priorityDragon)
                return@runs modMessage("shouldve worked!")
            }
        }
    }

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
            |currentRoom: ${DungeonUtils.currentRoom?.data?.name}, roomsPassed: ${DungeonUtils.passedRooms.map { it.data.name }}
            |Teammates: ${DungeonUtils.dungeonTeammates.joinToString { "§${it.clazz.colorCode}${it.name} (${it.clazz} [${it.clazzLvl}])" }}
            |TeammatesNoSelf: ${DungeonUtils.dungeonTeammatesNoSelf.map { it.name }}
            |LeapTeammates: ${DungeonUtils.leapTeammates.map { it.name }}
            |Blessings: ${Blessing.entries.joinToString { "${it.name}: ${it.current}" }}
            ${getChatBreak()}
        """.trimIndent(), "")
    }

    literal("getlocation").runs {
        modMessage("currentarea: ${LocationUtils.currentArea}, isDungeon ${DungeonUtils.inDungeons}, inKuudra: ${KuudraUtils.inKuudra} kuudratier: ${LocationUtils.kuudraTier}, dungeonfloor: ${DungeonUtils.floorNumber}")
    }

    literal("kuudrainfo").runs {
        modMessage("""
            ${getChatBreak()}
            |inKuudra: ${KuudraUtils.inKuudra}, tier: ${LocationUtils.kuudraTier}, phase: ${KuudraUtils.phase}
            |kuudraTeammates: ${KuudraUtils.kuudraTeammates.joinToString { it.playerName }}
            |giantZombies: ${KuudraUtils.giantZombies.joinToString { it.positionVector.toString() }}
            |supplies: ${KuudraUtils.supplies.joinToString()}
            |kuudraEntity: ${KuudraUtils.kuudraEntity}
            |builders: ${KuudraUtils.playersBuildingAmount}
            |build: ${KuudraUtils.buildDonePercentage}
            |buildingPiles: ${KuudraUtils.buildingPiles.joinToString { it.positionVector.toString() }}
            |missing: ${NoPre.missing}
            ${getChatBreak()}
        """.trimIndent(), "")

    }
    literal("simulate").runs { str: GreedyString ->
        mc.thePlayer.addChatMessage(ChatComponentText(str.string))
        PacketEvent.Receive(S02PacketChat(ChatComponentText(str.string))).postAndCatch()
    }

	literal("roomdata").runs {
        val room = DungeonUtils.currentRoom
        val roomCenter = getRoomCenter(mc.thePlayer.posX.toInt(), mc.thePlayer.posZ.toInt())
        val core = ScanUtils.getCore(roomCenter)
        modMessage(
            """
            ${getChatBreak()}
            Middle: ${roomCenter.x}, ${roomCenter.z}
            Room: ${DungeonUtils.currentRoomName}
            Core: $core
            Rotation: ${room?.rotation ?: "NONE"}
            Positions: ${room?.roomComponents?.joinToString { "(${it.x}, ${it.z})" } ?: "None"}
            ${getChatBreak()}
            """.trimIndent(), "")
        writeToClipboard(core.toString(), "§aCopied $core to clipboard!")
    }

    literal("relativecoords").runs {
        val block = mc.objectMouseOver?.blockPos ?: return@runs
        modMessage(
            """
            ${getChatBreak()}
            Middle: $block
            Relative Coords: ${DungeonUtils.currentRoom?.getRelativeCoords(block.toVec3())?.toString()}
            ${getChatBreak()}
            """.trimIndent(), "")
    }

    literal("getplayers").runs {
        val playerEntities = mc.theWorld?.playerEntities?.filter { it.isOtherPlayer() } ?: return@runs modMessage("No players found.")
        modMessage("Players: ${playerEntities.joinToString { it.name ?: "Unknown" }}")
    }

    literal("generatereadme").runs {
        writeToClipboard(generateFeatureList())
    }
}
