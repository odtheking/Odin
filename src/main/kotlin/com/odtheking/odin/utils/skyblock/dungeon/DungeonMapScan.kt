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

    var roomSize: Int = -1
        private set
    private var roomGap: Int = -1
    var startX: Int = -1
        private set
    var startY: Int = -1
        private set

    fun unload() {
        roomSize = -1
        roomGap = -1
        startX = -1
        startY = -1
    }

    fun ClientboundMapItemDataPacket.rescan() {
        if (!DungeonUtils.inClear || mapId().id and 1000 != 0) return
        val colors = colorPatch.getOrNull()?.mapColors() ?: return
        if (colors.size < MAP_SIZE * MAP_SIZE || colors[0] != EMPTY) return

        if (roomSize == -1 && !initLayout(colors)) return

        decorations.getOrNull()?.let { updatePlayers(it) }
        updateAll(colors)
    }

    private fun initLayout(colors: ByteArray): Boolean {
        val (start, size) = colors.indices.firstNotNullOfOrNull { i ->
            if (colors[i] != RoomType.ENTRANCE.mapColor) return@firstNotNullOfOrNull null
            val end = (i until colors.size).firstOrNull { colors[it] != RoomType.ENTRANCE.mapColor } ?: colors.size
            (end - i).takeIf { it == 16 || it == 18 }?.let { i to it }
        } ?: return false

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
        val tiles    = DungeonScan.tiles
        val halfRoom = roomSize ushr 1
        val connGap  = roomSize + ROOM_SPACING / 2

        fun px(x: Int, z: Int): Byte =
            if (x in 0 until MAP_SIZE && z in 0 until MAP_SIZE) colors[z * MAP_SIZE + x] else EMPTY

        fun doorTypeFromColor(col: Byte) = when (col) {
            MapCheckmark.QUESTION_MARK.mapColor -> DoorType.Wither
            RoomType.BLOOD.mapColor             -> DoorType.Blood
            RoomType.FAIRY.mapColor             -> DoorType.Fairy
            else                                -> DoorType.Normal
        }

        val roomTypeAt  = arrayOfNulls<RoomType>(36)
        val roomColAt   = ByteArray(36)
        val centerColAt = ByteArray(36)

        for (tz in 0..5) for (tx in 0..5) {
            val ox = startX + tx * roomGap
            val oz = startY + tz * roomGap

            val cornerCol = px(ox, oz)
            if (cornerCol != EMPTY) {
                val type = RoomType.fromMapColor(cornerCol)
                if (type != null && type != RoomType.UNKNOWN && type != RoomType.UNDISCOVERED) {
                    val idx = tx + tz * 6
                    roomTypeAt[idx]  = type
                    roomColAt[idx]   = cornerCol
                    centerColAt[idx] = px(ox + halfRoom, oz + halfRoom)
                }
            }

            if (tx < 5 && px(ox + connGap, oz) == EMPTY) {
                val dc = px(ox + connGap, oz + halfRoom)
                if (dc != EMPTY) ensureDoor(
                    chunkX   = -12 + 2 * tx + 1, chunkZ = -12 + 2 * tz,
                    pos      = IVec2(tx, tz),     rotation = DoorRotation.Horizontal,
                    type     = doorTypeFromColor(dc)
                )
            }

            if (tz < 5 && px(ox, oz + connGap) == EMPTY) {
                val dc = px(ox + halfRoom, oz + connGap)
                if (dc != EMPTY) ensureDoor(
                    chunkX   = -12 + 2 * tx, chunkZ = -12 + 2 * tz + 1,
                    pos      = IVec2(tx, tz), rotation = DoorRotation.Vertical,
                    type     = doorTypeFromColor(dc)
                )
            }
        }

        fun connectedRight(tx: Int, tz: Int) = px(startX + tx * roomGap + connGap, startY + tz * roomGap) != EMPTY
        fun connectedDown (tx: Int, tz: Int) = px(startX + tx * roomGap, startY + tz * roomGap + connGap) != EMPTY

        val visited = BooleanArray(36)
        val dirs    = arrayOf(1 to 0, -1 to 0, 0 to 1, 0 to -1)

        for (startIdx in 0 until 36) {
            val startType = roomTypeAt[startIdx] ?: continue
            if (visited[startIdx]) continue

            val component = ArrayList<Int>(4)
            val queue     = ArrayDeque<Int>()
            queue.add(startIdx)
            visited[startIdx] = true

            while (queue.isNotEmpty()) {
                val cur = queue.removeFirst()
                component.add(cur)
                val cx = cur % 6; val cz = cur / 6

                for ((dx, dz) in dirs) {
                    val nx = cx + dx; val nz = cz + dz
                    if (nx !in 0..5 || nz !in 0..5) continue
                    val nIdx = nx + nz * 6
                    if (visited[nIdx] || roomTypeAt[nIdx] != startType) continue

                    val connected = when {
                        dx ==  1 -> connectedRight(cx, cz)
                        dx == -1 -> connectedRight(nx, nz)
                        dz ==  1 -> connectedDown (cx, cz)
                        else     -> connectedDown (nx, nz)
                    }
                    if (!connected) continue
                    visited[nIdx] = true
                    queue.add(nIdx)
                }
            }

            val (shape, rotation) = DungeonRoom.inferLayout(
                positions = component.map { IVec2(it % 6, it / 6) }
            )

            val existingRooms = component
                .mapNotNull { tiles[it].room }
                .distinct()
                .filterNot { it is DungeonRoom.Collecting }

            val room: DungeonRoom = if (existingRooms.isEmpty()) {
                DungeonRoom.MapResolved(shape = shape, rotation = rotation, type = startType)
                    .also { mr ->
                        DungeonScan.rooms.add(mr)
                        component.forEach { idx ->
                            val tile = tiles[idx]
                            if (!mr.segments.contains(tile)) mr.segments.add(tile)
                        }
                    }
            } else {
                existingRooms.first()
            }

            for (idx in component) if (tiles[idx].room !== room) tiles[idx].room = room

            if (existingRooms.size > 1) {
                existingRooms.drop(1).forEach { other ->
                    if (other !== room && other is DungeonRoom.MapResolved) DungeonScan.rooms.remove(other)
                }
            }

            room.checkmark = MapCheckmark.UNDISCOVERED
            for (tileIdx in component) {
                val roomCol   = roomColAt[tileIdx]
                val centerCol = centerColAt[tileIdx]
                if (centerCol == roomCol && room.checkmark.equalsOneOf(MapCheckmark.UNDISCOVERED, MapCheckmark.QUESTION_MARK)) {
                    room.checkmark = MapCheckmark.NONE
                } else {
                    MapCheckmark.fromMapColor(centerCol)?.let { room.checkmark = it }
                }
            }
        }
    }

    private fun ensureDoor(chunkX: Int, chunkZ: Int, pos: IVec2, rotation: DoorRotation, type: DoorType) {
        val chunkPos = IVec2(chunkX, chunkZ)
        val existing = DungeonScan.doors[chunkPos]
        if (existing == null || existing.rotation != rotation || existing.position != pos) {
            DungeonScan.doors[chunkPos] = DungeonDoor(position = pos, rotation = rotation, type = type)
            return
        }
        if (existing.type != type && type != DoorType.Normal) existing.type = type
    }

    init {
        on<WorldEvent.Load> { unload() }
        onReceive<ClientboundMapItemDataPacket> { rescan() }
    }
}