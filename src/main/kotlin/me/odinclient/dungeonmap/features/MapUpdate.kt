package me.odinclient.dungeonmap.features

import me.odinclient.OdinClient.Companion.mc
import me.odinclient.dungeonmap.core.DungeonPlayer
import me.odinclient.dungeonmap.core.map.*
import me.odinclient.utils.skyblock.dungeon.DungeonUtils
import me.odinclient.utils.skyblock.dungeon.map.MapUtils
import me.odinclient.utils.skyblock.dungeon.map.MapUtils.mapX
import me.odinclient.utils.skyblock.dungeon.map.MapUtils.mapZ
import me.odinclient.utils.skyblock.dungeon.map.MapUtils.yaw
import net.minecraft.client.network.NetworkPlayerInfo
import net.minecraft.entity.player.EnumPlayerModelParts
import net.minecraft.init.Blocks
import net.minecraft.util.BlockPos
import net.minecraft.util.StringUtils

object MapUpdate {
    fun calibrate() {
        MapUtils.startCorner = when {
            DungeonUtils.isFloor(1) -> Pair(22, 11)
            DungeonUtils.isFloor(2, 3) -> Pair(11, 11)
            DungeonUtils.isFloor(4) && Dungeon.rooms.size > 25 -> Pair(5, 16)
            Dungeon.rooms.size == 30 -> Pair(16, 5)
            Dungeon.rooms.size == 25 -> Pair(11, 11)
            else -> Pair(5, 5)
        }

        MapUtils.roomSize = if (DungeonUtils.isFloor(1,2,3) || Dungeon.rooms.size == 24) 18 else 16

        MapUtils.coordMultiplier = (MapUtils.roomSize + 4.0) / Dungeon.roomSize

        MapUtils.calibrated = true
    }

    fun preloadHeads() {
        val tabEntries = Dungeon.getDungeonTabList() ?: return
        for (i in listOf(5, 9, 13, 17, 1)) {
            // Accessing the skin locations to load in skin
            tabEntries[i].first.locationSkin
        }
    }

    fun getPlayers() {
        val tabEntries = Dungeon.getDungeonTabList() ?: return
        Dungeon.dungeonTeammates.clear()
        var iconNum = 0
        for (i in listOf(5, 9, 13, 17, 1)) {
            with(tabEntries[i]) {
                val name = StringUtils.stripControlCodes(second).trim().split(" ")[0]
                if (name != "") {
                    Dungeon.dungeonTeammates[name] = DungeonPlayer(first.locationSkin).apply {
                        icon = "icon-$iconNum"
                        renderHat = mc.theWorld.getPlayerEntityByName(name)?.isWearing(EnumPlayerModelParts.HAT) == true
                    }
                    iconNum++
                }
            }
        }
    }

    fun updatePlayers(tabEntries: List<Pair<NetworkPlayerInfo, String>>) {
        if (Dungeon.dungeonTeammates.isEmpty()) return

        var iconNum = 0
        for (i in listOf(5, 9, 13, 17, 1)) {
            val tabText = StringUtils.stripControlCodes(tabEntries[i].second).trim()
            val text = tabText.split(" ")
            val name = if (text.size < 2) text[0] else text[1]
            if (name == "") continue
            Dungeon.dungeonTeammates[name]?.run {
                dead = tabText.contains("(DEAD)")
                if (dead) {
                    icon = ""
                } else {
                    icon = "icon-$iconNum"
                    iconNum++
                }
            }
        }

        val decor = MapUtils.getMapData()?.mapDecorations ?: return
        Dungeon.dungeonTeammates.forEach { (name, player) ->
            if (name == mc.thePlayer.name) {
                player.yaw = mc.thePlayer.rotationYawHead
            } else {
                decor.entries.find { (icon, _) -> icon == player.icon }?.let { (_, vec4b) ->
                    player.mapX = vec4b.mapX
                    player.mapZ = vec4b.mapZ
                    player.yaw = vec4b.yaw
                }
            }
        }
    }

    fun updateRooms() {
        val mapColors = MapUtils.getMapData()?.colors ?: return

        val startX = MapUtils.startCorner.first + (MapUtils.roomSize shr 1)
        val startZ = MapUtils.startCorner.second + (MapUtils.roomSize shr 1)
        val increment = (MapUtils.roomSize shr 1) + 2

        for (x in 0..10) {
            for (z in 0..10) {

                val mapX = startX + x * increment
                val mapZ = startZ + z * increment

                if (mapX >= 128 || mapZ >= 128) continue

                val room = Dungeon.dungeonList[z * 11 + x]

                room.state = when (mapColors[(mapZ shl 7) + mapX].toInt()) {
                    0, 85, 119 -> RoomState.UNDISCOVERED
                    18 -> if (room is Room) when (room.data.type) {
                        RoomType.BLOOD -> RoomState.DISCOVERED
                        RoomType.PUZZLE -> RoomState.FAILED
                        else -> room.state
                    } else RoomState.DISCOVERED
                    30 -> if (room is Room) when (room.data.type) {
                        RoomType.ENTRANCE -> RoomState.DISCOVERED
                        else -> RoomState.GREEN
                    } else room.state
                    34 -> RoomState.CLEARED
                    else -> RoomState.DISCOVERED
                }
            }
        }
    }

    fun updateDoors() {
        for ((door, pos) in Dungeon.doors) {
            if (!door.opened && mc.theWorld.getChunkFromChunkCoords(door.x shr 4, door.z shr 4).isLoaded) {
                if (mc.theWorld.getBlockState(BlockPos(door.x, 69, door.z)).block == Blocks.air) {
                    val room = Dungeon.dungeonList[pos.first + pos.second * 11]
                    if (room is Door && room.type == DoorType.WITHER) {
                        room.opened = true
                        door.opened = true
                    }
                }
            }
        }
    }
}