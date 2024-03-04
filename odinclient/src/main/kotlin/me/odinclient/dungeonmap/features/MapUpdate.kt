package me.odinclient.dungeonmap.features

import me.odinclient.dungeonmap.core.DungeonPlayer
import me.odinclient.utils.skyblock.dungeon.map.MapUtils
import me.odinclient.utils.skyblock.dungeon.map.MapUtils.mapX
import me.odinclient.utils.skyblock.dungeon.map.MapUtils.mapZ
import me.odinclient.utils.skyblock.dungeon.map.MapUtils.yaw
import me.odinmain.OdinMain.mc
import me.odinmain.utils.noControlCodes
import me.odinmain.utils.skyblock.dungeon.Room
import me.odinmain.utils.skyblock.dungeon.RoomState
import me.odinmain.utils.skyblock.dungeon.RoomType
import net.minecraft.client.network.NetworkPlayerInfo
import kotlin.math.roundToInt

object MapUpdate {
    fun preloadHeads() {
        val tabEntries = MapUtils.getDungeonTabList() ?: return
        for (i in listOf(5, 9, 13, 17, 1)) {
            // Accessing the skin locations to load in skin
            tabEntries[i].first.locationSkin
        }
    }

    fun getPlayers() {
        val tabEntries = MapUtils.getDungeonTabList() ?: return
        Dungeon.dungeonTeammatesFmap.clear()
        var iconNum = 0
        for (i in listOf(5, 9, 13, 17, 1)) {
            with(tabEntries[i]) {
                val name = second.noControlCodes.trim().substringAfterLast("] ").split(" ")[0]
                if (name != "") {
                    Dungeon.dungeonTeammatesFmap[name] = DungeonPlayer(first.locationSkin).apply {
                        mc.theWorld.getPlayerEntityByName(name)?.let { setData(it) }
                        colorPrefix = second.substringBefore(name, "f").last()
                        this.name = name
                        icon = "icon-$iconNum"
                    }
                    iconNum++
                }
            }
        }
    }

    fun updatePlayers(tabEntries: List<Pair<NetworkPlayerInfo, String>>) {
        if (Dungeon.dungeonTeammatesFmap.isEmpty()) return
        // Update map icons
        var iconNum = 0
        for (i in listOf(5, 9, 13, 17, 1)) {
            val tabText = tabEntries[i].second.noControlCodes.trim()
            val name = tabText.substringAfterLast("] ").split(" ")[0]
            if (name == "") continue
            Dungeon.dungeonTeammatesFmap[name]?.run {
                dead = tabText.contains("(DEAD)")
                if (dead) {
                    icon = ""
                } else {
                    icon = "icon-$iconNum"
                    iconNum++
                }
                if (!playerLoaded) {
                    mc.theWorld.getPlayerEntityByName(name)?.let { setData(it) }
                }
            }
        }

        val decor = MapUtils.getMapData()?.mapDecorations ?: return
        Dungeon.dungeonTeammatesFmap.forEach { (name, player) ->
            if (name == mc.thePlayer.name) {
                player.yaw = mc.thePlayer.rotationYawHead
                player.mapX =
                    ((mc.thePlayer.posX - DungeonScan.startX + 15) * MapUtils.coordMultiplier + MapUtils.startCorner.first).roundToInt()
                player.mapZ =
                    ((mc.thePlayer.posZ - DungeonScan.startZ + 15) * MapUtils.coordMultiplier + MapUtils.startCorner.second).roundToInt()
                return@forEach
            }
            decor.entries.find { (icon, _) -> icon == player.icon }?.let { (_, vec4b) ->
                player.mapX = vec4b.mapX
                player.mapZ = vec4b.mapZ
                player.yaw = vec4b.yaw
            }
        }
    }

    fun updateRooms() {
        val mapColors = MapUtils.getMapData()?.colors ?: return

        val startX = MapUtils.startCorner.first + (MapUtils.mapRoomSize shr 1)
        val startZ = MapUtils.startCorner.second + (MapUtils.mapRoomSize shr 1)
        val increment = (MapUtils.mapRoomSize shr 1) + 2

        for (x in 0..10) {
            for (z in 0..10) {

                val mapX = startX + x * increment
                val mapZ = startZ + z * increment

                if (mapX >= 128 || mapZ >= 128) continue

                val room = Dungeon.Info.dungeonList[z * 11 + x]

                val newState = when (mapColors[(mapZ shl 7) + mapX].toInt()) {
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

                if (newState != room.state) {
                    room.state = newState
                }
            }
        }
    }
}