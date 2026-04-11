package com.odtheking.odin.utils.skyblock.dungeon.map.scan

import com.odtheking.odin.events.WorldEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.events.core.onReceive
import com.odtheking.odin.features.impl.dungeon.map.Vec2i
import com.odtheking.odin.utils.IVec2
import com.odtheking.odin.utils.equalsOneOf
import com.odtheking.odin.utils.modMessage
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import com.odtheking.odin.utils.skyblock.dungeon.map.tile.*
import net.minecraft.network.protocol.game.ClientboundMapItemDataPacket
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.saveddata.maps.MapDecoration
import net.minecraft.world.level.saveddata.maps.MapDecorationTypes
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.jvm.optionals.getOrNull

object DungeonMapScan {

    private const val MAP_SIZE = 128
    private const val ROOM_SPACING = 4
    private const val EMPTY: Byte = 0

    var roomGap = -1

    var roomSize = -1
        private set

    var startX = -1
        private set

    var startY = -1
        private set

    val tiles: Array<MapScanTile> = Array(36) { index ->
        MapScanTile(position = IVec2(x = index % 6, z = index / 6))
    }

    val rooms: CopyOnWriteArrayList<MapScanRoom> = CopyOnWriteArrayList()
    val doors: ConcurrentHashMap<IVec2, DungeonDoor> = ConcurrentHashMap()

    init {
        on<WorldEvent.Load> {
            repeat(36) { index ->
                tiles[index] = MapScanTile(position = IVec2(x = index % 6, z = index / 6))
            }
            rooms.clear()
            doors.clear()
            roomSize = -1
            roomGap = -1
            startX = -1
            startY = -1
        }

        onReceive<ClientboundMapItemDataPacket> {
            if (!DungeonUtils.inClear || mapId().id and 1000 != 0) return@onReceive

            decorations.getOrNull()?.let { updatePlayers(it) }
            val colors = colorPatch.getOrNull()?.mapColors() ?: return@onReceive
            if (colors.size < MAP_SIZE * MAP_SIZE || colors[0] != EMPTY) return@onReceive

            if (roomSize == -1 && !initLayout(colors)) return@onReceive
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
                roomSize = length
                roomGap = roomSize + ROOM_SPACING
                startX = (index % MAP_SIZE) % roomGap
                startY = (index / MAP_SIZE) % roomGap

                if (startX == 0) startX = 22 // f1
                if (startY == 0) startY = 22 // entrance

                modMessage("Dungeon map layout initialized: roomSize=$roomSize, startX=$startX, startY=$startY")
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
                mapPos = Vec2i(decoration.x.toInt(), decoration.y.toInt())
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
    }

    private fun mapTiles(colors: ByteArray, roomTypes: Array<RoomType?>, roomColors: ByteArray, centerColors: ByteArray) {
        val halfRoom = roomSize / 2
        val connectionGap = roomSize + ROOM_SPACING / 2

        for (index in 0 until 36) {
            val tileX = index % 6
            val tileZ = index / 6

            val originX = startX + tileX * roomGap
            val originZ = startY + tileZ * roomGap

            val cornerColor = getPx(colors, originX, originZ)

            if (cornerColor != EMPTY) {
                val type = RoomType.fromMapColor(cornerColor)
                if (type != null && type != RoomType.UNKNOWN && type != RoomType.UNDISCOVERED) {
                    roomTypes[index] = type
                    roomColors[index] = cornerColor
                    centerColors[index] = getPx(colors, originX + halfRoom, originZ + halfRoom)
                }
            }

            if (tileX < 5 && getPx(colors, originX + connectionGap, originZ) == EMPTY) {
                val doorColor = getPx(colors, originX + connectionGap, originZ + halfRoom)
                if (doorColor != EMPTY) addOrFixDoor(IVec2(tileX, tileZ), DoorRotation.Horizontal, DoorType.fromColor(doorColor))
            }

            if (tileZ < 5 && getPx(colors, originX, originZ + connectionGap) == EMPTY) {
                val doorColor = getPx(colors, originX + halfRoom, originZ + connectionGap)
                if (doorColor != EMPTY) addOrFixDoor(IVec2(tileX, tileZ), DoorRotation.Vertical, DoorType.fromColor(doorColor))
            }
        }
    }

    private fun processRooms(colors: ByteArray, roomTypes: Array<RoomType?>, roomColors: ByteArray, centerColors: ByteArray) {
        val visited = BooleanArray(36)
        val directions = arrayOf(1 to 0, -1 to 0, 0 to 1, 0 to -1)

        val connectionGap = roomSize + ROOM_SPACING / 2

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

                for ((dx, dz) in directions) {
                    val nextX = currentX + dx
                    val nextZ = currentZ + dz

                    if (nextX !in 0..5 || nextZ !in 0..5) continue

                    val nextIndex = nextX + nextZ * 6
                    if (visited[nextIndex] || roomTypes[nextIndex] != roomType) continue

                    val connected = when {
                        dx == 1 -> getPx(colors, startX + currentX * roomGap + connectionGap, startY + currentZ * roomGap) != EMPTY
                        dx == -1 -> getPx(colors, startX + nextX * roomGap + connectionGap, startY + nextZ * roomGap) != EMPTY
                        dz == 1 -> getPx(colors, startX + currentX * roomGap, startY + currentZ * roomGap + connectionGap) != EMPTY
                        else -> getPx(colors, startX + nextX * roomGap, startY + nextZ * roomGap + connectionGap) != EMPTY
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
        val existingRoom = tiles[segments[0]].room
        val room = if (existingRoom != null && existingRoom.type == roomType) existingRoom
        else {
            MapScanRoom(roomType, IVec2(segments.minOf { it % 6 }, segments.minOf { it / 6 })).also { newRoom ->
                rooms.add(newRoom)
                for (index in segments) {
                    tiles[index].room = newRoom
                    newRoom.addSegment(tiles[index])
                }
                newRoom.inferLayout()
            }
        }

        for (index in segments) {
            val roomColor = roomColors[index]
            val centerColor = centerColors[index]

            if (centerColor == roomColor && room.checkmark.equalsOneOf(MapCheckmark.UNDISCOVERED, MapCheckmark.QUESTION_MARK)) room.checkmark = MapCheckmark.NONE
            else MapCheckmark.fromMapColor(centerColor)?.let { room.checkmark = it }
        }
    }

    fun playerRenderPosition(entity: Player?, mapPos: Vec2i): Pair<Float, Float> {
        entity?.let {
            val mapX = (it.x.toFloat() + 200f) * roomGap / 32f
            val mapZ = (it.z.toFloat() + 200f) * roomGap / 32f
            return mapX to mapZ
        }

        val pixelX = (mapPos.x + 128) / 2f - startX
        val pixelY = (mapPos.z + 128) / 2f - startY
        return pixelX to pixelY
    }

    private fun getPx(colors: ByteArray, x: Int, z: Int): Byte {
        return if (x in 0..<MAP_SIZE && z in 0..<MAP_SIZE) colors[z * MAP_SIZE + x] else EMPTY
    }
    private fun addOrFixDoor(position: IVec2, rotation: DoorRotation, type: DoorType) {
        val chunkPos = IVec2(-12 + 2 * position.x + rotation.offset.x, -12 + 2 * position.z + rotation.offset.z)
        val originIndex = position.x + position.z * 6
        val destPos = IVec2(position.x + rotation.offset.x, position.z + rotation.offset.z)
        val destIndex = (destPos.x + destPos.z * 6)
        doors.getOrPut(chunkPos) { DungeonDoor(position, rotation, type, originIndex, destIndex) }.type = type
    }
}