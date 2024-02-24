package me.odinclient.dungeonmap.features

import me.odinclient.dungeonmap.features.DungeonScan.scan
import me.odinclient.features.impl.dungeon.MapModule
import me.odinclient.utils.skyblock.dungeon.map.ScanUtils.getCore
import me.odinmain.OdinMain.mc
import me.odinmain.utils.equalsOneOf
import me.odinmain.utils.skyblock.dungeon.*
import me.odinmain.utils.skyblock.dungeon.ScanUtils.getRoomData
import me.odinmain.utils.skyblock.getChatBreak
import me.odinmain.utils.skyblock.modMessage
import net.minecraft.init.Blocks
import net.minecraft.util.BlockPos

/**
 * Handles everything related to scanning the dungeon. Running [scan] will update the instance of [Dungeon].
 *
 * @author Harry282
 */
object DungeonScan {

    const val roomSize = 32

    /**
     * The starting coordinates to start scanning (the north-west corner).
     */
    const val startX = -185
    const val startZ = -185

    private var lastScanTime = 0L
    var isScanning = false
    var hasScanned = false

    val shouldScan: Boolean
        get() = MapModule.autoScan && !isScanning && !hasScanned && System.currentTimeMillis() - lastScanTime >= 250 && DungeonUtils.inDungeons

    fun scan() {
        isScanning = true
        var allChunksLoaded = true

        // Scans the dungeon in a 11x11 grid.
        for (x in 0..10) {
            for (z in 0..10) {
                // Translates the grid index into world position.
                val xPos = startX + x * (roomSize shr 1)
                val zPos = startZ + z * (roomSize shr 1)

                if (!mc.theWorld.getChunkFromChunkCoords(xPos shr 4, zPos shr 4).isLoaded) {
                    // The room being scanned has not been loaded in.
                    allChunksLoaded = false
                    continue
                }

                // This room has already been added in a previous scan.
                if (Dungeon.Info.dungeonList[x + z * 11] !is Unknown) continue

                scanRoom(xPos, zPos, z, x)?.let {
                    Dungeon.Info.dungeonList[z * 11 + x] = it
                }
            }
        }

        if (allChunksLoaded) {
            hasScanned = true
            if (MapModule.scanChatInfo && MapModule.enabled) {
                modMessage("""
                    ${getChatBreak()}
                    §9Puzzles (§c${Dungeon.Info.puzzles.size}§9): §d${Dungeon.Info.puzzles.joinToString("§7, §d")}
                    §6Trap: §3${Dungeon.Info.trapType}
                    §8Wither Doors: §7${Dungeon.Info.witherDoors - 1}
                    §7Total Secrets: §b${Dungeon.Info.secretCount} ${if (Dungeon.Info.uniqueRooms.any { it.data.name == "Mini Rails" }) "\n§aThis map has trinity, the abiphone contact! The room is called Mini Rails" else ""}
                    ${getChatBreak()}
                """.trimIndent(), prefix = false)
            }
        }
        isScanning = false
    }

    private fun scanRoom(x: Int, z: Int, row: Int, column: Int): Tile? {
        val height = mc.theWorld.getChunkFromChunkCoords(x shr 4, z shr 4).getHeightValue(x and 15, z and 15)
        if (height == 0) return null

        val rowEven = row and 1 == 0
        val columnEven = column and 1 == 0

        return when {
            // Scanning a room
            rowEven && columnEven -> {
                val roomCore = getCore(x, z)
                Room(x, z, getRoomData(roomCore) ?: return null).apply {
                    core = roomCore
                    // Checks if a room with the same name has already been scanned.
                    val duplicateRoom = Dungeon.Info.uniqueRooms.firstOrNull { it.data.name == data.name }

                    if (duplicateRoom == null) {
                        // Adds room information if no duplicate was found
                        Dungeon.Info.uniqueRooms.add(this)
                        Dungeon.Info.cryptCount += data.crypts
                        Dungeon.Info.secretCount += data.secrets
                        when (data.type) {
                            RoomType.TRAP -> Dungeon.Info.trapType = data.name.split(" ")[0]
                            RoomType.PUZZLE -> Dungeon.Info.puzzles.add(data.name)
                            else -> {}
                        }
                    } else if (x < duplicateRoom.x || (x == duplicateRoom.x && z < duplicateRoom.z)) {
                        // Ensures the room stored in uniqueRooms is the furthest south-east.
                        Dungeon.Info.uniqueRooms.remove(duplicateRoom)
                        Dungeon.Info.uniqueRooms.add(this)
                    }
                }
            }

            // Can only be the center "block" of a 2x2 room.
            !rowEven && !columnEven -> {
                Dungeon.Info.dungeonList[column - 1 + (row - 1) * 11].let {
                    if (it is Room) Room(x, z, it.data).apply { isSeparator = true } else null
                }
            }

            // Doorway between rooms
            // Old trap has a single block at 82
            height.equalsOneOf(74, 82) -> {
                Door(x, z).apply {
                    // Finds door type from door block
                    type = when (mc.theWorld.getBlockState(BlockPos(x, 69, z)).block) {
                        Blocks.coal_block -> {
                            Dungeon.Info.witherDoors++
                            DoorType.WITHER
                        }

                        Blocks.monster_egg -> DoorType.ENTRANCE
                        Blocks.stained_hardened_clay -> DoorType.BLOOD
                        else -> DoorType.NORMAL
                    }
                }
            }

            // Connection between large rooms
            else -> {
                Dungeon.Info.dungeonList[if (rowEven) row * 11 + column - 1 else (row - 1) * 11 + column].let {
                    if (it !is Room) {
                        null
                    } else if (it.data.type == RoomType.ENTRANCE) {
                        Door(x, z).apply { type = DoorType.ENTRANCE }
                    } else {
                        Room(x, z, it.data).apply { isSeparator = true }
                    }
                }
            }
        }
    }
}