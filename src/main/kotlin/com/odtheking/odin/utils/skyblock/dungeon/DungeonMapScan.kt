package com.odtheking.odin.utils.skyblock.dungeon

import com.odtheking.odin.events.WorldEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.events.core.onReceive
import com.odtheking.odin.features.impl.dungeon.map.Vec2i
import com.odtheking.odin.utils.IVec2
import com.odtheking.odin.utils.equalsOneOf
import com.odtheking.odin.utils.skyblock.dungeon.door.DoorRotation
import com.odtheking.odin.utils.skyblock.dungeon.door.DoorType
import com.odtheking.odin.utils.skyblock.dungeon.door.DungeonDoor
import com.odtheking.odin.utils.skyblock.dungeon.room.DungeonRoom
import com.odtheking.odin.utils.skyblock.dungeon.room.MapCheckmark
import com.odtheking.odin.utils.skyblock.dungeon.room.RoomType
import net.minecraft.network.protocol.game.ClientboundMapItemDataPacket
import net.minecraft.world.level.saveddata.maps.MapDecoration
import net.minecraft.world.level.saveddata.maps.MapDecorationTypes
import kotlin.jvm.optionals.getOrNull

object DungeonMapScan {

    private const val MAP_SIZE    = 128
    private const val ROOM_SPACING = 4
    private const val EMPTY: Byte  = 0

    private var roomGap: Int = -1

    var roomSize: Int = -1
        private set

    var startX: Int = -1
        private set

    var startY: Int = -1
        private set

    init {
        on<WorldEvent.Load> {
            roomSize = -1
            roomGap = -1
            startX = -1
            startY = -1
        }
        onReceive<ClientboundMapItemDataPacket> {
            if (!DungeonUtils.inClear || mapId().id and 1000 != 0) return@onReceive

            val colors = colorPatch.getOrNull()?.mapColors() ?: return@onReceive
            if (colors.size < MAP_SIZE * MAP_SIZE || colors[0] != EMPTY) return@onReceive

            if (roomSize == -1 && !initLayout(colors)) {
                return@onReceive
            }

            decorations.getOrNull()?.let {
                updatePlayers(it)
            }
            updateAll(colors)
        }
    }

    private fun initLayout(colors: ByteArray): Boolean {
        var start = -1
        var size = -1

        // opinionated, but I think this is significantly nicer
        // because it is simpler to understand as it only uses basic loops instead of tons of stdlib functions

        for (index in 0..<colors.size) {
            if (colors[index] != RoomType.ENTRANCE.mapColor) {
                continue
            }

            var end = index
            while (end < colors.size && colors[end] == RoomType.ENTRANCE.mapColor) {
                end++
            }

            val length = end - index
            if (length == 16 || length == 18) {
                start = index
                size = length
                break
            }
        }

        if (start == -1) {
            return false
        }

        roomSize = size
        roomGap = roomSize + ROOM_SPACING
        startX = (start % MAP_SIZE) % roomGap
        startY = (start / MAP_SIZE) % roomGap
        return true
    }

    private fun updatePlayers(decorations: List<MapDecoration>) {
        val playerIterator = DungeonUtils.dungeonTeammatesNoSelf.iterator()

        decorations.forEach { decoration ->
            if (decoration.type.value() == MapDecorationTypes.FRAME.value()) return@forEach

            playerIterator.asSequence().firstOrNull { !it.isDead }?.apply {
                mapPos = Vec2i(decoration.x.toInt(), decoration.y.toInt())
                yaw = decoration.rot() * 360 / 16f
            }
        }
    }

    private fun updateAll(colors: ByteArray) {
        val tiles = DungeonWorldScan.tiles
        val halfRoom = roomSize ushr 1
        val connGap = roomSize + ROOM_SPACING / 2

        val roomTypeAt = arrayOfNulls<RoomType>(36)
        val roomColorAt = ByteArray(36)
        val centerColorAt = ByteArray(36)

        for (i in 0..<36) {
            val tileZ = i / 6
            val tileX = i % 6

            val ox = startX + tileX * roomGap
            val oz = startY + tileZ * roomGap

            val cornerColor = getPx(colors, ox, oz)
            if (cornerColor != EMPTY) {
                val type = RoomType.fromMapColor(cornerColor)
                if (type != null && type != RoomType.UNKNOWN && type != RoomType.UNDISCOVERED) {
                    val idx = tileX + tileZ * 6
                    roomTypeAt[idx] = type
                    roomColorAt[idx] = cornerColor
                    centerColorAt[idx] = getPx(colors, ox + halfRoom, oz + halfRoom)
                }
            }

            if (tileX < 5 && getPx(colors, ox + connGap, oz) == EMPTY) {
                val doorColor = getPx(colors, ox + connGap, oz + halfRoom)
                if (doorColor != EMPTY) {
                    addOrFixDoor(
                        position = IVec2(tileX, tileZ),
                        rotation = DoorRotation.Horizontal,
                        type = DoorType.fromColor(doorColor)
                    )
                }
            }

            if (tileZ < 5 && getPx(colors, ox, oz + connGap) == EMPTY) {
                val doorColor = getPx(colors, ox + halfRoom, oz + connGap)
                if (doorColor != EMPTY) {
                    addOrFixDoor(
                        position = IVec2(tileX, tileZ),
                        rotation = DoorRotation.Vertical,
                        type = DoorType.fromColor(doorColor)
                    )
                }
            }
        }

        val visited = BooleanArray(36)
        val dirs = arrayOf(1 to 0, -1 to 0, 0 to 1, 0 to -1)

        for (startIndex in 0 until 36) {
            val startType = roomTypeAt[startIndex] ?: continue
            if (visited[startIndex]) continue

            val component = ArrayList<Int>(4)
            val queue = ArrayDeque<Int>()

            queue.add(startIndex)
            visited[startIndex] = true

            while (queue.isNotEmpty()) {
                val current = queue.removeFirst()
                component.add(current)

                val currX = current % 6
                val currY = current / 6

                for ((dx, dz) in dirs) {
                    val nextX = currX + dx
                    val nextZ = currY + dz
                    if (nextX !in 0..<6 || nextZ !in 0..<6) continue

                    val nextIndex = nextX + nextZ * 6
                    if (visited[nextIndex] || roomTypeAt[nextIndex] != startType) continue

                    val connected = when {
                        dx == 1 -> getPx(colors, startX + currX * roomGap + connGap, startY + currY * roomGap) != EMPTY
                        dx == -1 -> getPx(colors, startX + nextX * roomGap + connGap, startY + nextZ * roomGap) != EMPTY
                        dz == 1 -> getPx(colors, startX + currX * roomGap, startY + currY * roomGap + connGap) != EMPTY
                        else -> getPx(colors, startX + nextX * roomGap, startY + nextZ * roomGap + connGap) != EMPTY
                    }

                    if (!connected) continue
                    visited[nextIndex] = true
                    queue.add(nextIndex)
                }
            }

            val (shape, rotation) = DungeonRoom.inferLayout(positions = component.map { IVec2(it % 6, it / 6) })

            val existingRooms = component
                .mapNotNull { tiles[it].room }
                .distinct()
                .filterNot { it is DungeonRoom.Collecting }

            val room: DungeonRoom = if (existingRooms.isEmpty()) {
                val room = DungeonRoom.MapResolved(shape = shape, rotation = rotation, type = startType)
                DungeonWorldScan.rooms.add(room)

                component.forEach { idx ->
                    val tile = tiles[idx]
                    if (!room.segments.contains(tile)) room.segments.add(tile)
                }
                room
            } else {
                existingRooms.first()
            }

            for (index in component) {
                if (tiles[index].room !== room) tiles[index].room = room
            }

            if (existingRooms.size > 1) {
                existingRooms.drop(1).forEach { other ->
                    if (other !== room && other is DungeonRoom.MapResolved) {
                        DungeonWorldScan.rooms.remove(other)
                    }
                }
            }

            room.checkmark = MapCheckmark.UNDISCOVERED

            for (tileIndex in component) {
                val roomColor = roomColorAt[tileIndex]
                val centerColor = centerColorAt[tileIndex]
                if (centerColor == roomColor && room.checkmark.equalsOneOf(MapCheckmark.UNDISCOVERED, MapCheckmark.QUESTION_MARK)) {
                    room.checkmark = MapCheckmark.NONE
                } else {
                    MapCheckmark.fromMapColor(centerColor)?.let {
                        room.checkmark = it
                    }
                }
            }
        }
    }

    private fun getPx(colors: ByteArray, x: Int, z: Int): Byte {
        return if (x in 0..<MAP_SIZE && z in 0..<MAP_SIZE) colors[z * MAP_SIZE + x] else EMPTY
    }

    private fun addOrFixDoor(position: IVec2, rotation: DoorRotation, type: DoorType) {
        val chunkPos = IVec2(
            -12 + 2 * position.x + rotation.offset.x,
            -12 + 2 * position.z + rotation.offset.z,
        )

        val existing = DungeonWorldScan.doors[chunkPos]

        if (existing == null || existing.rotation != rotation || existing.position != position) {
            DungeonWorldScan.doors[chunkPos] = DungeonDoor(position = position, rotation = rotation, type = type)
            return
        }
        if (existing.type != type && type != DoorType.Normal) {
            existing.type = type
        }
    }
}