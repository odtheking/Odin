package com.odtheking.odin.commands

import com.github.stivais.commodore.Commodore
import com.github.stivais.commodore.utils.GreedyString
import com.odtheking.odin.OdinMod
import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.events.ChatPacketEvent
import com.odtheking.odin.features.ModuleManager
import com.odtheking.odin.features.impl.floor7.MelodyMessage.melodyWebSocket
import com.odtheking.odin.features.impl.floor7.WitherDragonState
import com.odtheking.odin.features.impl.floor7.WitherDragons
import com.odtheking.odin.features.impl.floor7.WitherDragonsEnum
import com.odtheking.odin.features.impl.nether.NoPre
import com.odtheking.odin.features.impl.render.ClickGUIModule.webSocketUrl
import com.odtheking.odin.features.impl.render.PlayerSize
import com.odtheking.odin.features.impl.render.PlayerSize.DEV_SERVER
import com.odtheking.odin.features.impl.render.PlayerSize.buildDevBody
import com.odtheking.odin.utils.*
import com.odtheking.odin.utils.network.WebUtils.fetchJson
import com.odtheking.odin.utils.network.WebUtils.postData
import com.odtheking.odin.utils.skyblock.KuudraUtils
import com.odtheking.odin.utils.skyblock.LocationUtils
import com.odtheking.odin.utils.skyblock.PartyUtils
import com.odtheking.odin.utils.skyblock.Supply
import com.odtheking.odin.utils.skyblock.dungeon.Blessing
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils.getRelativeCoords
import com.odtheking.odin.utils.skyblock.dungeon.ScanUtils
import com.odtheking.odin.utils.skyblock.dungeon.ScanUtils.getRoomCenter
import com.odtheking.odin.utils.ui.rendering.NVGRenderer
import kotlinx.coroutines.launch
import com.google.gson.JsonObject
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.world.phys.BlockHitResult
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
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
            PlayerSize.updateCustomProperties()
            modMessage("Updated devs.")
        }
    }

    literal("deletedevs").runs {
        PlayerSize.randoms.clear()
    }

    suspend fun getUUID(name: String): String? {
        val req = fetchJson<JsonObject>("https://api.mojang.com/users/profiles/minecraft/$name")
        val uuid = req.getOrNull()?.get("id")?.asString ?: run {
            modMessage("Failed to fetch UUID for $name")
            return null
        }
        return Uuid.parse(uuid).toString()
    }

    literal("adddev").runs { name: String, password: String, xSize: Float?, ySize: Float?, zSize: Float? ->
        val x = xSize ?: 0.6f
        val y = ySize ?: 0.6f
        val z = zSize ?: 0.6f
        modMessage("Sending data... name: $name, x: $x, y: $y, z: $z")
        OdinMod.scope.launch {
            val uuid = getUUID(name) ?: return@launch
            modMessage("Sending data for UUID: $uuid ($name, x: $x, y: $y, z: $z)")
            modMessage(postData(DEV_SERVER, buildDevBody(uuid, Colors.WHITE, x, y, z, false, " ", password)).getOrNull())
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
                            |currentRoom: ${DungeonUtils.currentRoom?.data?.name}, roomsPassed: ${DungeonUtils.passedRooms.map { it.data.name }}
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

            val spawnInfo = buildString {
                append(" §8│ §7Spawned: §fx${dragon.timesSpawned}")
            }

            val uuid = buildString {
                append(" §8│ §7UUID: §f${dragon.entityUUID}")
            }

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

    literal("roomdata").runs {
        val player = mc.player ?: return@runs
        val vec2 = Vec2(player.x.toInt(), player.z.toInt())
        val chunk = mc.level?.getChunk(vec2.x shr 4, vec2.z shr 4) ?: return@runs
        val roomCenter = getRoomCenter(vec2.x, vec2.z)
        val room = ScanUtils.scanRoom(roomCenter)
        val core = ScanUtils.getCore(roomCenter)
        modMessage(
            """
            Middle: ${roomCenter.x}, ${roomCenter.z}
            Room: ${ScanUtils.coreToRoomData[core]?.name}
            Core: $core
            Rotation: ${room?.rotation ?: "NONE"}
            Positions: ${room?.roomComponents?.joinToString { "(${it.x}, ${it.z})" } ?: "None"}
            Height: ${ScanUtils.getTopLayerOfRoom(vec2, chunk)}
            """.trimIndent(), "")
        setClipboardContent(core.toString())
        modMessage("§aCopied $core to clipboard!")
    }

    literal("relative").runs {
        mc.hitResult?.let {
            if (it !is BlockHitResult) return@runs
            DungeonUtils.currentRoom?.getRelativeCoords(it.blockPos)?.let { vec2 ->
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