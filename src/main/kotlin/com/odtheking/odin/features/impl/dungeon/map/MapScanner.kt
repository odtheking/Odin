package com.odtheking.odin.features.impl.dungeon.map

import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.utils.Vec2
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import com.odtheking.odin.utils.skyblock.dungeon.ScanUtils
import com.odtheking.odin.utils.skyblock.dungeon.tiles.*
import net.minecraft.core.BlockPos
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Blocks
import java.util.concurrent.CopyOnWriteArraySet

object MapScanner {
    val topLeftRoom = Vec2i(-185, -185)
    var loadedAllRooms = false
    var roomsList: Array<MapRoom.Tile> = Array(36) {
        val x = it / 6
        val z = it % 6
        MapRoom.Tile(null, topLeftRoom.add(x * 32, z * 32))
    }
        private set

    val list: Array<Any?> = Array(121) { null }
    val doorPositions: MutableSet<Vec2i> = CopyOnWriteArraySet()

    val rooms = CopyOnWriteArraySet<MapRoom>()
    val doors = CopyOnWriteArraySet<Door>()
    var blood: MapRoom? = null

    fun unload() {
        rooms.clear()
        roomsList = Array(36) {
            val x = it / 6
            val z = it % 6
            MapRoom.Tile(null, topLeftRoom.add(x * 32, z * 32))
        }
        list.fill(null)
        doorPositions.clear()
        doors.clear()
        loadedAllRooms = false
        blood = null
    }

    fun scan(world: Level) {
        if (loadedAllRooms) {
            scanDoors(world)
            return
        }

        scanRooms()
        scanRoomRotations()
        scanDoors(world)

        loadedAllRooms = checkIfFullyLoaded()
    }

    private fun scanRoomRotations() {
        rooms.forEach { room ->
            if (room.rotation != Rotations.NONE) return@forEach

            val roomData = room.data ?: return@forEach

            val requiredTiles = when (room.shape) {
                RoomShape.S4x1 -> 7
                RoomShape.S3x1 -> 5
                else -> 0
            }

            if (requiredTiles > 0 && room.tiles.size != requiredTiles) return@forEach

            val tempRoom = Room(
                data = roomData,
                roomComponents = room.tiles.map { RoomComponent(it.pos.x, it.pos.z) }.toMutableSet()
            )

            ScanUtils.updateRotation(tempRoom, room.height)

            if (tempRoom.rotation != Rotations.NONE) room.rotation = tempRoom.rotation
        }
    }

    private fun checkIfFullyLoaded(): Boolean {
        val size = DungMap.mapSize ?: return false

        if (SpecialColumn.column != -1 && rooms.count { it.data?.type == RoomType.PUZZLE && it.rotation != Rotations.NONE } != DungeonUtils.puzzleCount)
            return false

        val zMax = if (SpecialColumn.column != -1) size.z - 1 else size.z
        for (x in 0 until size.x) {
            for (z in 0 until zMax) {
                val owner = (list[Vec2i(x, z).roomListIndex()] as? MapRoom.Tile)?.owner ?: return false
                if (owner.data == null) return false
                if (owner.rotation == Rotations.NONE) return false
            }
        }

        return rooms.none { room -> room.data != null && room.rotation == Rotations.NONE }
    }

    private fun scanRooms() {
        for (x in 0..5) {
            for (z in 0..5) {
                val tile = Vec2i(x, z)
                val listIndex = tile.roomListIndex()
                val curr = topLeftRoom.add(tile.multiply(32))

                val existingTile = list[listIndex] as? MapRoom.Tile
                if (existingTile?.owner?.data != null) continue

                ScanUtils.scanRoom(Vec2(curr.x, curr.z))?.let { room ->
                    val chunk = mc.level?.getChunk(curr.x shr 4, curr.z shr 4) ?: return@let
                    val roomHeight = ScanUtils.getTopLayerOfRoom(Vec2(curr.x, curr.z), chunk)

                    val mapItemRoom = existingTile?.owner
                    if (mapItemRoom != null) {
                        mapItemRoom.data = room.data
                        mapItemRoom.type = room.data.type
                        mapItemRoom.shape = room.data.shape
                        if (mapItemRoom.height == 0) mapItemRoom.height = roomHeight
                        if (room.rotation != Rotations.NONE) mapItemRoom.rotation = room.rotation
                        return@let
                    }

                    val found = rooms.find { it.data?.name == room.data.name }
                    if (found != null) {
                        if (found.tiles.none { it.pos == curr }) {
                            found.roomTile(curr)?.let { roomTile ->
                                list[listIndex] = roomTile
                            }
                        }
                    } else {
                        val newRoom = MapRoom(room.data, roomHeight)
                        rooms.add(newRoom)
                        newRoom.roomTile(curr)?.let { roomTile ->
                            list[listIndex] = roomTile
                        }
                    }
                }
            }
        }
    }

    private fun scanDoors(world: Level) {
        fun handleDoor(pos: Vec2i, a: Vec2i, b: Vec2i) {
            if (pos in doorPositions) return

            val tileA = list[a.x * 11 + a.z]
            val tileB = list[b.x * 11 + b.z]

            val rooms = listOfNotNull(tileA as? MapRoom.Tile, tileB as? MapRoom.Tile)
            if (rooms.size != 2) return

            val chunk = world.getChunk(pos.x shr 4, pos.z shr 4)
            val height = ScanUtils.getTopLayerOfRoom(Vec2(pos.x, pos.z), chunk)

            val x = (pos.x + 185) / 16
            val z = (pos.z + 185) / 16
            val listIndex = x * 11 + z

            if (height !in arrayOf(73, 81)) {
                if (height <= 73) return

                if (rooms.any { it.owner?.data?.type == RoomType.ENTRANCE }) {
                    val tile = Door(pos, Door.Type.NORMAL, rooms.toMutableList())
                    doors.add(tile)
                    doorPositions.add(pos)
                    list[listIndex] = tile
                    return
                }

                rooms[0].owner?.separator(pos)?.let { list[listIndex] = it }
                return
            }

            val type = when (world.getBlockState(BlockPos(pos.x, 69, pos.z)).block) {
                Blocks.COAL_BLOCK -> {
                    rooms.forEach { it.owner?.rushRoom = true }
                    Door.Type.WITHER
                }
                Blocks.RED_TERRACOTTA -> Door.Type.BLOOD
                else -> Door.Type.NORMAL
            }

            val tile = Door(pos, type, rooms.toMutableList())
            doors.add(tile)
            doorPositions.add(pos)
            list[listIndex] = tile
        }

        for (a in 0..5) {
            for (b in 0..4) {
                val goingRight = Vec2i(topLeftRoom.x + a * 32, topLeftRoom.z + 16 + 32 * b)
                val x1 = (goingRight.x + 185) / 16
                val z1 = (goingRight.z + 185) / 16
                handleDoor(goingRight, Vec2i(x1, z1 + 1), Vec2i(x1, z1 - 1))

                val goingDown = Vec2i(goingRight.z, goingRight.x)
                val x2 = (goingDown.x + 185) / 16
                val z2 = (goingDown.z + 185) / 16
                handleDoor(goingDown, Vec2i(x2 + 1, z2), Vec2i(x2 - 1, z2))
            }
        }

        for (a in 0..4) {
            for (b in 0..4) {
                val x = a * 2 + 1
                val z = b * 2 + 1
                val pos = Vec2i(topLeftRoom.x + x * 16, topLeftRoom.z + z * 16)

                val surroundingTiles = listOf(
                    list[(x - 1) * 11 + z - 1],
                    list[(x - 1) * 11 + z + 1],
                    list[(x + 1) * 11 + z - 1],
                    list[(x + 1) * 11 + z + 1]
                )

                if (surroundingTiles.any { it !is MapRoom.Tile }) continue
                val mapRoom = surroundingTiles[0] as MapRoom.Tile
                if (mapRoom.owner?.tiles?.any { it.pos == pos } == true) continue

                val chunk = world.getChunk(pos.x shr 4, pos.z shr 4)
                val height = ScanUtils.getTopLayerOfRoom(Vec2(pos.x, pos.z), chunk)

                if (height > 73 && height != 140)
                    mapRoom.owner?.separator(pos)?.let { list[x * 11 + z] = it }
            }
        }
    }
}