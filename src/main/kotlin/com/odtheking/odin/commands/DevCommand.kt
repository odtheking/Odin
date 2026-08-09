package com.odtheking.odin.commands

import com.github.stivais.commodore.Commodore
import com.github.stivais.commodore.utils.GreedyString
import com.odtheking.odin.OdinMod
import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.events.ChatPacketEvent
import com.odtheking.odin.features.ModuleManager
import com.odtheking.odin.features.impl.boss.MelodyMessage.melodyWebSocket
import com.odtheking.odin.features.impl.boss.WitherDragonState
import com.odtheking.odin.features.impl.boss.WitherDragons
import com.odtheking.odin.features.impl.boss.WitherDragonsEnum
import com.odtheking.odin.features.impl.dungeon.map.DungeonScan
import com.odtheking.odin.features.impl.dungeon.map.WorldScan
import com.odtheking.odin.features.impl.nether.NoPre
import com.odtheking.odin.features.impl.render.ClickGUIModule.webSocketUrl
import com.odtheking.odin.features.impl.render.PlayerSize
import com.odtheking.odin.features.impl.render.PlayerSize.DEV_SERVER
import com.odtheking.odin.features.impl.render.PlayerSize.buildDevBody
import com.odtheking.odin.utils.*
import com.odtheking.odin.utils.network.WebUtils.postData
import com.odtheking.odin.utils.skyblock.KuudraUtils
import com.odtheking.odin.utils.skyblock.LocationUtils
import com.odtheking.odin.utils.skyblock.PartyUtils
import com.odtheking.odin.utils.skyblock.Supply
import com.odtheking.odin.utils.skyblock.dungeon.Blessing
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import com.odtheking.odin.utils.skyblock.dungeon.Floor
import com.odtheking.odin.utils.ui.rendering.NVGRenderer
import kotlinx.coroutines.launch
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.world.phys.BlockHitResult

val devCommand = Commodore("oddev") {

    literal("ws") {
        literal("connect").runs { lobby: String ->
            melodyWebSocket.connect("${webSocketUrl}${lobby}")
        }
    }

    literal("getitem").runs {
        modMessage("Item in hand: ${mc.player?.mainHandItem?.customData}")
    }

    literal("giveaotv").runs { tuners: Int? ->
        sendCommand("give @p minecraft:diamond_shovel[minecraft:custom_name={\"text\":\"Aspect Of The Void\",\"color\":\"dark_purple\"},minecraft:custom_data={ethermerge:1,\"tuned_transmission\":${tuners ?: 0}}]")
    }

    literal("simulate").runs { greedyString: GreedyString ->
        ChatPacketEvent(greedyString.string, Component.literal(greedyString.string)).postAndCatch()
        modMessage("§8Simulated message: ${greedyString.string}")
    }

    literal("updatedevs").runs {
        OdinMod.scope.launch {
            devMessage(PlayerSize.updateCustomProperties())
        }
    }

    literal("deletedevs").runs {
        PlayerSize.clearCustomProperties()
    }

    literal("adddev").runs { name: String, password: String, xSize: Float?, ySize: Float?, zSize: Float? ->
        val devBody = buildDevBody(name, xSize ?: 0.6f, ySize ?: 0.6f, zSize ?: 0.6f, " ", password)
        modMessage("Sending $devBody")
        OdinMod.scope.launch {
            modMessage(postData(DEV_SERVER, devBody).getOrNull())
        }
    }

    literal("generatefeaturelist").runs {
        fun generateFeatureList(): String {
            val featureList = StringBuilder()

            for ((category, modulesInCategory) in ModuleManager.modulesByCategory) {
                featureList.appendLine("Category: ${category.name}")
                for (module in modulesInCategory.sortedByDescending {
                    NVGRenderer.textWidth(it.name, 16f, NVGRenderer.defaultFont)
                }) featureList.appendLine("- ${module.name}: ${module.description}")
                featureList.appendLine()
            }
            return featureList.toString()
        }
        setClipboardContent(generateFeatureList())
        modMessage("Generated feature list and copied to clipboard.")
    }

    literal("debug").executable {

        param("type").suggests { setOf("kuudra", "dungeon", "none", "party") }

        runs { type: String? ->
            modMessage(
                """
                |Version: ${OdinMod.version}
                |Location: ${LocationUtils.currentArea.displayName}
                ${
                    when (type) {
                        "kuudra" -> """
                            |inKuudra: ${KuudraUtils.inKuudra}, tier: ${KuudraUtils.kuudraTier}, phase: ${KuudraUtils.phase}
                            |kuudraTeammates: ${KuudraUtils.freshers.map { it.key }}
                            |giantZombies: ${KuudraUtils.giantZombies.joinToString { it.position().toString() }}
                            |supplies: ${Supply.entries.joinToString { "${it.name} -> ${it.isActive}" }}
                            |kuudraEntity: ${KuudraUtils.kuudraEntity}
                            |builders: ${KuudraUtils.playersBuildingAmount}
                            |build: ${KuudraUtils.buildDonePercentage}
                            |buildingPiles: ${KuudraUtils.buildingPiles.joinToString { it.position().toString() }}
                            |missing: ${NoPre.missing}
                        """.trimIndent()

                        "dungeon" -> """
                            |inDungeons: ${DungeonUtils.inDungeons}
                            |InBoss: ${DungeonUtils.inBoss}
                            |Floor: ${DungeonUtils.floor?.name}
                            |Score: ${DungeonUtils.score}
                            |Secrets: (${DungeonUtils.secretCount} - ${DungeonUtils.neededSecretsAmount} - ${DungeonUtils.totalSecrets} - ${DungeonUtils.knownSecrets}) 
                            |mimicKilled: ${DungeonUtils.mimicKilled}
                            |Deaths: ${DungeonUtils.deathCount}, Crypts: ${DungeonUtils.cryptCount}
                            |BonusScore: ${DungeonUtils.getBonusScore}, isPaul: ${DungeonUtils.isPaul}
                            |OpenRooms: ${DungeonUtils.openRoomCount}, CompletedRooms: ${DungeonUtils.completedRoomCount} ${DungeonUtils.percentCleared}%, Blood Done: ${DungeonUtils.bloodDone}, Total: ${DungeonUtils.totalRooms}
                            |Puzzles (${DungeonUtils.puzzleCount}): ${DungeonUtils.puzzles.joinToString { "${it.name} (${it.status.toString()})" }}
                            |DungeonTime: ${DungeonUtils.dungeonTime}
                            |currentDungeonPlayer: ${DungeonUtils.currentDungeonPlayer.name}, ${DungeonUtils.currentDungeonPlayer.clazz}, ${DungeonUtils.currentDungeonPlayer.isDead}
                            |doorOpener: ${DungeonUtils.doorOpener}
                            |currentRoom: ${WorldScan.currentRoom?.data?.name}, roomsPassed: ${DungeonScan.rooms.filter { it.walkedInto }.map { it.data?.name }}
                            |Teammates: ${DungeonUtils.dungeonTeammates.joinToString { "§${it.clazz.colorCode}${it.name} (${it.clazz} [${it.clazzLvl}])§r" }}
                            |TeammatesNoSelf: ${DungeonUtils.dungeonTeammatesNoSelf.map { it.name }}
                            |LeapTeammates: ${DungeonUtils.leapTeammates.map { it.name }}
                            |Blessings: ${Blessing.entries.joinToString { "${it.name}: ${it.current}" }}
                        """.trimIndent()

                        "party" -> """
                            |Party > ${PartyUtils.isInParty}, ${PartyUtils.partyLeader}, ${PartyUtils.members}
                        """.trimIndent()

                        else -> ""
                    }
                }
            """.trimIndent(), ""
            )
        }
    }

    literal("dragons").runs {
        WitherDragonsEnum.entries.forEach { dragon ->
            val stateInfo = buildString {
                when (dragon.state) {
                    WitherDragonState.ALIVE -> append("§a✓ ALIVE")
                    WitherDragonState.SPAWNING -> append("§e⚡ SPAWNING")
                    WitherDragonState.DEAD -> append("§7✗ DEAD")
                }
                append(" §8│ §eIn §f${(dragon.timeToSpawn / 20f).toFixed()}§es")
            }

            val spawnInfo = buildString { append(" §8│ §7Spawned: §fx${dragon.timesSpawned}") }
            val uuid = buildString { append(" §8│ §7UUID: §f${dragon.entityUUID}") }

            val flags = buildString {
                val flagList = mutableListOf<String>()
                if (WitherDragons.priorityDragon == dragon) flagList.add("§6§l➤ PRIORITY")
                if (dragon.isSprayed) flagList.add("§d✓ Sprayed")
                if (flagList.isNotEmpty()) {
                    append(" §8│ ")
                    append(flagList.joinToString(" §8│ "))
                }
            }

            modMessage("§${dragon.colorCode}§l${dragon.name.padEnd(7)}§r $stateInfo$spawnInfo$uuid$flags")
        }
    }

    literal("roomdata").runs { x: Int?, z: Int? ->
        val player = mc.player ?: return@runs
        val tileX = x ?: ((player.blockX + 201) shr 5)
        val tileZ = z ?: ((player.blockZ + 201) shr 5)

        val roomPos = IVec2(tileX * 32 - 185, tileZ * 32 - 185)
        val chunk = mc.level?.getChunk(roomPos.x shr 4, roomPos.z shr 4) ?: return@runs
        val core = WorldScan.getRoomCore(chunk, roomPos)
        val room = DungeonScan.tiles[tileX + tileZ * 6].room ?: return@runs modMessage("§cNo room data found for the current tile.")

        modMessage(
            """
            Middle: ${roomPos.x} ${roomPos.z}
            Room: ${room.name} ${room.shape}
            Core: ${core.first} height: ${core.second}
            Rotation: ${room.rotation ?: "NONE"} ${room.clayPos}
            Positions: ${room.tiles.joinToString { tile -> "${room.getRealPosition(tile.x, tile.z)}" }}
            """.trimIndent(), ""
        )
        setClipboardContent(core.first.toString())
        modMessage("§aCopied §f${core.first} §ato clipboard!")
    }

    literal("setfloor").runs { floorNumber: Int ->
        Floor.entries.find { it.floorNumber == floorNumber }?.let { floor ->
            DungeonScan.initClient(floor)
        } ?: modMessage("§cInvalid floor number: $floorNumber. Valid floors are 1-7.")
    }

    literal("relative").runs {
        mc.hitResult?.let {
            if (it !is BlockHitResult) return@runs
            WorldScan.currentRoom?.getRelativeCoords(it.blockPos)?.let { vec2 ->
                modMessage("Relative coords: ${vec2.x}, ${vec2.z}")
            }
        }
    }

    literal("setblock").executable {
        param("type").suggests { BuiltInRegistries.BLOCK.entrySet().map { it.value.descriptionId.replace("block.minecraft.", "") } }

        runs { type: String ->
            mc.hitResult?.let {
                if (it !is BlockHitResult) return@runs
                sendCommand("setblock ${it.blockPos.x} ${it.blockPos.y} ${it.blockPos.z} minecraft:$type")
            }
        }
    }

    literal("copy").runs { greedyString: GreedyString ->
        setClipboardContent(greedyString.string)
        modMessage("§aCopied to clipboard!")
    }
}