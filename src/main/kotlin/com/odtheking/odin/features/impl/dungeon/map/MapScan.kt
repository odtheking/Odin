package com.odtheking.odin.features.impl.dungeon.map

import com.odtheking.odin.events.CheckmarkUpdateEvent
import com.odtheking.odin.events.LevelEvent
import com.odtheking.odin.events.MapUpdateEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.events.core.onReceive
import com.odtheking.odin.features.impl.dungeon.map.tile.*
import com.odtheking.odin.utils.IVec2
import com.odtheking.odin.utils.equalsOneOf
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import net.minecraft.network.protocol.game.ClientboundMapItemDataPacket
import net.minecraft.world.level.saveddata.maps.MapDecoration
import net.minecraft.world.level.saveddata.maps.MapDecorationTypes
import kotlin.jvm.optionals.getOrNull

object MapScan {

    private const val MAP_SIZE = 128
    private const val EMPTY: Byte = 0

    private var isInit = false

    init {
        on<LevelEvent.Load> { isInit = false }

        onReceive<ClientboundMapItemDataPacket> {
            if (!DungeonUtils.inClear || mapId().id and 1000 != 0) return@onReceive

            decorations.getOrNull()?.let { updatePlayers(it) }
            val colors = colorPatch.getOrNull()?.mapColors() ?: return@onReceive
            if (colors.size < MAP_SIZE * MAP_SIZE || colors[0] != EMPTY) return@onReceive

            if (!isInit && !initLayout(colors)) return@onReceive
            if (DungeonScan.roomSize == -1) return@onReceive
            updateAll(colors)
        }
    }

    private fun initLayout(colors: ByteArray): Boolean {
        for ((index, color) in colors.withIndex()) {
            if (color != RoomType.ENTRANCE.mapColor) continue

            var end = index
            while (end < colors.size && colors[end] == color) end++

            val length = end - index
            if (length == 16 || length == 18) {
                DungeonScan.roomSize = length
                DungeonScan.roomGap = length + DungeonScan.ROOM_SPACING
                DungeonScan.startX = (index % MAP_SIZE) % DungeonScan.roomGap
                DungeonScan.startY = (index / MAP_SIZE) % DungeonScan.roomGap

                if (DungeonScan.startX == 0) DungeonScan.startX = 22 // f1
                if (DungeonScan.startY == 0) DungeonScan.startY = 22 // entrance

                isInit = true

                if ((DungeonUtils.isFloor(6, 5) && DungeonScan.startX == 5) || (DungeonUtils.isFloor(4) && DungeonScan.startX == 5))
                    SpecialColumn.column = 5

                return true
            }
        }
        return false
    }

    private fun updatePlayers(decorations: List<MapDecoration>) {
        val iterator = DungeonUtils.dungeonTeammatesNoSelf.iterator()

        for (decoration in decorations) {
            if (decoration.type.value() == MapDecorationTypes.FRAME.value()) continue

            iterator.asSequence().firstOrNull { !it.isDead }?.apply {
                mapPos = IVec2(decoration.x.toInt(), decoration.y.toInt())
                yaw = decoration.rot() * 360 / 16f
            }
        }
    }

    private fun updateAll(colors: ByteArray) {
        val roomTypes = arrayOfNulls<RoomType>(36)
        val roomColors = ByteArray(36)
        val centerColors = ByteArray(36)
        mapTiles(colors, roomTypes, roomColors, centerColors)
        processRooms(colors, roomTypes, roomColors, centerColors)
        MapUpdateEvent.postAndCatch()
    }

    private fun mapTiles(colors: ByteArray, roomTypes: Array<RoomType?>, roomColors: ByteArray, centerColors: ByteArray) {
        val halfRoom = DungeonScan.roomSize / 2
        val connectionGap = DungeonScan.connectionGap
        val sideCheckOffset = 4

        for (index in 0 until 36) {
            val tileX = index % 6
            val tileZ = index / 6

            val originX = DungeonScan.startX + tileX * DungeonScan.roomGap
            val originZ = DungeonScan.startY + tileZ * DungeonScan.roomGap

            val cornerColor = getPx(colors, originX, originZ)

            if (cornerColor != EMPTY) {
                val type = RoomType.fromMapColor(cornerColor)
                if (type != null && type != RoomType.UNKNOWN && type != RoomType.UNDISCOVERED) {
                    roomTypes[index] = type
                    roomColors[index] = cornerColor
                    centerColors[index] = getPx(colors, originX + halfRoom, originZ + halfRoom)
                }
            }

            if (tileX < 5) {
                val doorColor = getPx(colors, originX + connectionGap, originZ + halfRoom)
                val sideColor = getPx(colors, originX + connectionGap, originZ + halfRoom - sideCheckOffset)
                if (sideColor == EMPTY && doorColor != EMPTY)
                    addOrFixDoor(IVec2(tileX, tileZ), DoorRotation.Horizontal, DoorType.fromColor(doorColor))
            }

            if (tileZ < 5) {
                val doorColor = getPx(colors, originX + halfRoom, originZ + connectionGap)
                val sideColor = getPx(colors, originX + halfRoom - sideCheckOffset, originZ + connectionGap)
                if (sideColor == EMPTY && doorColor != EMPTY)
                    addOrFixDoor(IVec2(tileX, tileZ), DoorRotation.Vertical, DoorType.fromColor(doorColor))
            }
        }
    }

    private fun processRooms(colors: ByteArray, roomTypes: Array<RoomType?>, roomColors: ByteArray, centerColors: ByteArray) {
        val visited = BooleanArray(36)
        val connectionGap = DungeonScan.connectionGap

        for (startIndex in 0 until 36) {
            val roomType = roomTypes[startIndex] ?: continue
            if (visited[startIndex]) continue

            val components = ArrayList<Int>()
            val queue = ArrayDeque<Int>()

            queue.add(startIndex)
            visited[startIndex] = true

            while (queue.isNotEmpty()) {
                val currentIndex = queue.removeFirst()
                components.add(currentIndex)

                val currentX = currentIndex % 6
                val currentZ = currentIndex / 6

                for ((dx, dz) in DungeonScan.directions) {
                    val nextX = currentX + dx
                    val nextZ = currentZ + dz

                    if (nextX !in 0..5 || nextZ !in 0..5) continue

                    val nextIndex = nextX + nextZ * 6
                    if (visited[nextIndex] || roomTypes[nextIndex] != roomType) continue

                    val connected = when {
                        dx == 1 -> getPx(colors, DungeonScan.startX + currentX * DungeonScan.roomGap + connectionGap, DungeonScan.startY + currentZ * DungeonScan.roomGap) != EMPTY
                        dx == -1 -> getPx(colors, DungeonScan.startX + nextX * DungeonScan.roomGap + connectionGap, DungeonScan.startY + nextZ * DungeonScan.roomGap) != EMPTY
                        dz == 1 -> getPx(colors, DungeonScan.startX + currentX * DungeonScan.roomGap, DungeonScan.startY + currentZ * DungeonScan.roomGap + connectionGap) != EMPTY
                        else -> getPx(colors, DungeonScan.startX + nextX * DungeonScan.roomGap, DungeonScan.startY + nextZ * DungeonScan.roomGap + connectionGap) != EMPTY
                    }

                    if (!connected) continue

                    visited[nextIndex] = true
                    queue.add(nextIndex)
                }
            }

            buildRoom(components, roomType, roomColors, centerColors)
        }
    }

    private fun buildRoom(segments: List<Int>, roomType: RoomType, roomColors: ByteArray, centerColors: ByteArray) {
        val existingRoom = segments.firstNotNullOfOrNull { DungeonScan.tiles[it].room }

        val room = if (existingRoom != null) {
            for (index in segments) {
                if (DungeonScan.tiles[index].room != existingRoom) {
                    DungeonScan.tiles[index].room = existingRoom
                    existingRoom.addSegment(DungeonScan.tiles[index])
                }
            }
            if (existingRoom.highestBlock == null) existingRoom.inferLayoutFromMap()
            existingRoom
        } else {
            DungeonRoom(roomType, IVec2(segments.minOf { it % 6 }, segments.minOf { it / 6 })).also { newRoom ->
                DungeonScan.rooms.add(newRoom)
                for (index in segments) {
                    DungeonScan.tiles[index].room = newRoom
                    newRoom.addSegment(DungeonScan.tiles[index])
                }
                newRoom.inferLayoutFromMap()
            }
        }

        for (index in segments) {
            val roomColor = roomColors[index]
            val centerColor = centerColors[index]

            val newCheckmark = when {
                centerColor == roomColor && room.checkmark.equalsOneOf(MapCheckmark.UNDISCOVERED, MapCheckmark.QUESTION_MARK) -> MapCheckmark.NONE
                else -> MapCheckmark.fromMapColor(centerColor)
            }

            if (newCheckmark != null && newCheckmark != room.checkmark) {
                room.checkmark = newCheckmark
                CheckmarkUpdateEvent(room, newCheckmark).postAndCatch()
            }
        }
    }

    private fun addOrFixDoor(position: IVec2, rotation: DoorRotation, doorType: DoorType) {
        DungeonScan.doors.getOrPut(IVec2(-12 + 2 * position.x + rotation.offset.x, -12 + 2 * position.z + rotation.offset.z))
        { DungeonDoor(position, rotation, doorType) }.type = doorType
    }

    private fun getPx(colors: ByteArray, x: Int, z: Int): Byte =
        if (x in 0..<MAP_SIZE && z in 0..<MAP_SIZE) colors[z * MAP_SIZE + x] else EMPTY
}