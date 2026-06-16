package com.odtheking.odin.features.impl.dungeon.map

import com.odtheking.odin.utils.equalsOneOf
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import com.odtheking.odin.utils.skyblock.dungeon.tiles.RoomShape
import com.odtheking.odin.utils.skyblock.dungeon.tiles.RoomType
import net.minecraft.network.protocol.game.ClientboundMapItemDataPacket
import net.minecraft.world.level.saveddata.maps.MapDecorationTypes
import kotlin.jvm.optionals.getOrNull

object DungMap {
    var mapCenter: Vec2i = Vec2i(0, 0)
    var startCoords: Vec2i? = null
    var mapSize: Vec2i? = null
    var roomSize: Int? = null
    var shouldScan = false

    private const val MAP_SIZE = 128
    private const val EMPTY: Byte = 0

    fun unload() {
        mapCenter = Vec2i(0, 0)
        shouldScan = false
        startCoords = null
        mapSize = null
        roomSize = null
    }

    fun onChunkLoad() {
        shouldScan = true
        if (mapSize == null) {
            mapSize = when (DungeonUtils.floor?.floorNumber) {
                0 -> Vec2i(4, 4)
                1 -> Vec2i(4, 5)
                2, 3 -> Vec2i(5, 5)
                else -> Vec2i(6, 6)
            }
        }
    }

    fun rescanMapItem(packet: ClientboundMapItemDataPacket) {
        if (!DungeonUtils.inClear || packet.mapId().id and 1000 != 0) return
        updatePlayerPositions(packet)

        val colors = packet.colorPatch.getOrNull()?.mapColors() ?: return
        if (colors.size < MAP_SIZE * MAP_SIZE || colors[0] != EMPTY) return

        if (startCoords == null && !initializeMapCoordinates(colors)) return

        updateRoomTiles(colors)
        updateRoomStates(colors)
        updateDoorStates(colors)

        if (mapSize != null) SpecialColumn.updateSpecialColumn()
    }

    private fun initializeMapCoordinates(colors: ByteArray): Boolean {
        val (greenStart, greenLength) = findGreenRoom(colors)
        if (!greenLength.equalsOneOf(16, 18)) return false

        val (start, center, size) = when (DungeonUtils.floor?.floorNumber) {
            0 -> Triple(Vec2i(22, 22), Vec2i(-137, -137), Vec2i(4, 4))
            1 -> Triple(Vec2i(22, 11), Vec2i(-137, -121), Vec2i(4, 5))
            2, 3 -> Triple(Vec2i(11, 11), Vec2i(-121, -121), Vec2i(5, 5))
            else -> calculateDynamicMapSize(greenStart, greenLength)
        }

        roomSize = greenLength
        startCoords = start
        mapCenter = center
        mapSize = size

        if ((DungeonUtils.isFloor(6, 5) && size.x == 6 && size.z == 6) || (DungeonUtils.isFloor(4) && size.x == 6 && size.z == 5))
            SpecialColumn.column = 5

        return true
    }

    private fun calculateDynamicMapSize(greenStart: Int, greenLength: Int): Triple<Vec2i, Vec2i, Vec2i> {
        val start = Vec2i((greenStart and 127) % (greenLength + 4), (greenStart shr 7) % (greenLength + 4))

        val extra = Vec2i(if (start.x == 5) 1 else 0, if (start.z == 5) 1 else 0)
        val size = Vec2i(5, 5).add(extra)
        val center = Vec2i(-121, -121).add(Vec2i(extra.x * 16, extra.z * 16))

        return Triple(start, center, size)
    }

    private fun updatePlayerPositions(packet: ClientboundMapItemDataPacket) {
        packet.decorations.getOrNull()?.let { decorations ->
            val playerIterator = DungeonUtils.dungeonTeammatesNoSelf.iterator()

            decorations.forEach { decoration ->
                if (decoration.type.value() == MapDecorationTypes.FRAME.value()) return@forEach

                val player = playerIterator.asSequence().firstOrNull { !it.isDead } ?: return@forEach
                player.mapPos = Vec2i(decoration.x.toInt(), decoration.y.toInt())
                player.yaw = decoration.rot() * 360 / 16f
            }
        }
    }

    private fun updateRoomTiles(colors: ByteArray) {
        val rs = roomSize ?: return
        val sc = startCoords ?: return
        val ms = mapSize ?: return

        val tileGrid = Array(ms.x) { Array(ms.z) { -1 } }
        var nextId = 0
        for (i in 0 until ms.x) {
            for (j in 0 until ms.z) {
                val idx = Vec2i(i, j).multiply(rs + 4).add(sc).mapIndex()
                if (idx < colors.size && colors[idx].toInt() != 0) {
                    tileGrid[i][j] = nextId++
                }
            }
        }

        val queue = ArrayDeque<Vec2i>()
        for (i in 0 until ms.x) for (j in 0 until ms.z)
            if (tileGrid[i][j] != -1) queue += Vec2i(i, j)

        val visited = Array(ms.x) { BooleanArray(ms.z) }
        while (queue.isNotEmpty()) {
            val cur = queue.removeFirst()
            if (visited[cur.x][cur.z]) continue
            visited[cur.x][cur.z] = true
            val id = tileGrid[cur.x][cur.z]

            val nr = cur.x + 1
            if (nr < ms.x && tileGrid[nr][cur.z] != -1 && !visited[nr][cur.z]) {
                val di = sc.add(Vec2i(rs + 1 + cur.x * (rs + 4), cur.z * (rs + 4) + 1)).mapIndex()
                if (di < colors.size && colors[di].toInt() != 0) {
                    tileGrid[nr][cur.z] = id
                    queue += Vec2i(nr, cur.z)
                }
            }

            val nb = cur.z + 1
            if (nb < ms.z && tileGrid[cur.x][nb] != -1 && !visited[cur.x][nb]) {
                val di = sc.add(Vec2i(cur.x * (rs + 4) + 1, rs + 1 + cur.z * (rs + 4))).mapIndex()
                if (di < colors.size && colors[di].toInt() != 0) {
                    tileGrid[cur.x][nb] = id
                    queue += Vec2i(cur.x, nb)
                }
            }
        }

        data class UniqueRoom(val id: Int, val coords: Vec2i, val tiles: MutableList<Vec2i>)
        val uniques = mutableListOf<UniqueRoom>()
        tileGrid.forEachIndexed { i, col ->
            col.forEachIndexed { j, id ->
                if (id == -1) return@forEachIndexed
                (uniques.find { it.id == id } ?: run {
                    val u = UniqueRoom(id, Vec2i(i, j), mutableListOf())
                    uniques.add(u)
                    u
                }).tiles.add(Vec2i(i, j))
            }
        }

        uniques.forEach { unique ->
            val idx = unique.coords.multiply(rs + 4).add(sc).mapIndex()
            val type = when (colors[idx].toInt()) {
                18 -> RoomType.BLOOD
                30 -> RoomType.ENTRANCE
                85 -> RoomType.UNKNOWN
                63 -> RoomType.NORMAL
                62 -> RoomType.TRAP
                66 -> RoomType.PUZZLE
                74 -> RoomType.CHAMPION
                82 -> RoomType.FAIRY
                else -> return@forEach
            }

            val shape = if (type == RoomType.UNKNOWN) RoomShape.UNKNOWN
            else when (unique.tiles.size) {
                1 -> RoomShape.S1x1
                2 -> RoomShape.S2x1
                3 -> {
                    val (a, b, c) = unique.tiles
                    if ((a.x == b.x && a.x == c.x) || (a.z == b.z && a.z == c.z)) RoomShape.S3x1 else RoomShape.L
                }
                4 -> {
                    val (a, b, c) = unique.tiles
                    if ((a.x == b.x && a.x == c.x) || (a.z == b.z && a.z == c.z)) RoomShape.S4x1 else RoomShape.S2x2
                }
                else -> return@forEach
            }

            val found = unique.tiles.firstNotNullOfOrNull { tile ->
                MapScanner.roomsList[tile.x * 6 + tile.z].owner
            }

            if (found != null) {
                if (type != RoomType.UNKNOWN && found.type == RoomType.UNKNOWN) {
                    found.type = type
                    found.shape = shape
                }
                unique.tiles.forEach { tile ->
                    val other = MapScanner.roomsList[tile.x * 6 + tile.z].owner
                    if (other != null && other != found) {
                        other.doors.forEach { found.doors.add(it) }
                        MapScanner.rooms.remove(other)
                    }
                    if (!found.places.contains(tile)) {
                        found.roomTile(tile.multiply(32).add(-185, -185))?.let { roomTile ->
                            MapScanner.list[tile.roomListIndex()] = roomTile
                        }
                    }
                }
                return@forEach
            }

            val room = MapRoom(type, shape)
            MapScanner.rooms.add(room)
            unique.tiles.forEach { tile ->
                room.roomTile(tile.multiply(32).add(-185, -185))?.let { roomTile ->
                    MapScanner.list[tile.roomListIndex()] = roomTile
                }
            }
        }
    }

    private fun updateRoomStates(colors: ByteArray) {
        val rs = roomSize ?: return
        val halfRoomSize = rs / 2
        val startCenter = startCoords?.add(Vec2i(halfRoomSize, halfRoomSize)) ?: return
        val tileSize = rs + 4

        MapScanner.rooms.forEach { room ->
            val topLeftPlacement = room.places.minWith { a, b ->
                a.x * 1000 + a.z - b.x * 1000 - b.z
            }

            var color = getColorAtPlacement(topLeftPlacement, startCenter, tileSize, colors).toInt()
            var placement = topLeftPlacement

            if (color == 0) {
                room.places.firstNotNullOfOrNull { testPlacement ->
                    val testColor = getColorAtPlacement(testPlacement, startCenter, tileSize, colors)
                    if (testColor != 0.toByte()) Pair(testPlacement, testColor) else null
                }?.let { (visiblePlacement, visibleColor) ->
                    placement = visiblePlacement
                    color = visibleColor.toInt()
                }
            }

            room.updateState(placement, color)
        }
    }

    private fun updateDoorStates(colors: ByteArray) {
        val rs = roomSize ?: return
        val halfRoomSize = rs / 2
        val startCenter = startCoords?.add(Vec2i(halfRoomSize, halfRoomSize)) ?: return

        for (a in 0..4) {
            for (b in 0..5) {
                val doorOffset = halfRoomSize + a * (rs + 4)
                val midRoomOffset = b * (rs + 4)

                val horizontalDoorIndex = startCenter.add(Vec2i(doorOffset, midRoomOffset)).mapIndex()
                if (horizontalDoorIndex < colors.size) {
                    (MapScanner.list[(a * 2 + 1) * 11 + b * 2] as? Door)?.let { door ->
                        updateDoorFromColor(door, colors[horizontalDoorIndex])
                    }
                }

                val verticalDoorIndex = startCenter.add(Vec2i(midRoomOffset, doorOffset)).mapIndex()
                if (verticalDoorIndex < colors.size) {
                    (MapScanner.list[(b * 2) * 11 + (a * 2 + 1)] as? Door)?.let { door ->
                        updateDoorFromColor(door, colors[verticalDoorIndex])
                    }
                }
            }
        }
    }

    fun findGreenRoom(mapData: ByteArray): Pair<Int, Int> {
        var start = -1
        var length = 0

        for (i in mapData.indices) {
            if (mapData[i].toInt() == 30) {
                if (length++ == 0) start = i
            } else {
                if (length >= 16) return start to length
                length = 0
            }
        }

        return start to length
    }

    private fun getColorAtPlacement(placement: Vec2i, startCenter: Vec2i, tileSize: Int, colors: ByteArray): Byte {
        val mapIndex = startCenter.add(placement.multiply(tileSize)).mapIndex()
        return if (mapIndex < colors.size) colors[mapIndex] else 0
    }

    private fun updateDoorFromColor(door: Door, color: Byte) {
        door.locked = when (color.toInt()) {
            119 -> {
                door.rooms.forEach { it.owner?.rushRoom = true }
                door.type = Door.Type.WITHER
                true
            }
            82 -> {
                door.rooms.forEach { it.owner?.rushRoom = true }
                false
            }
            18 -> {
                door.rooms.forEach { it.owner?.rushRoom = true }
                true
            }
            0 -> true
            else -> false
        }
    }

    fun calculateMapSize(): Vec2i = mapSize ?: Vec2i(6, 6)
}