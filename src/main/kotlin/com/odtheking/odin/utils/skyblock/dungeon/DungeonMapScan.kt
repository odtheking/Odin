package com.odtheking.odin.utils.skyblock.dungeon

import com.odtheking.odin.events.WorldEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.events.core.onReceive
import com.odtheking.odin.features.impl.dungeon.map.Vec2i
import com.odtheking.odin.utils.equalsOneOf
import com.odtheking.odin.utils.skyblock.dungeon.room.MapCheckmark
import com.odtheking.odin.utils.skyblock.dungeon.room.RoomType
import net.minecraft.network.protocol.game.ClientboundMapItemDataPacket
import net.minecraft.world.level.saveddata.maps.MapDecoration
import net.minecraft.world.level.saveddata.maps.MapDecorationTypes
import kotlin.jvm.optionals.getOrNull

object DungeonMapScan {

    private const val MAP_SIZE = 128
    private const val ROOM_SPACING = 4
    private const val EMPTY: Byte = 0

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
        updateRooms(colors)
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

    private fun updateRooms(colors: ByteArray) {
        val tiles = DungeonScan.tiles
        for (tileZ in 0..5) for (tileX in 0..5) {
            val tile = tiles[tileX + tileZ * 6]

            val rx = startX + tileX * roomGap
            val rz = startY + tileZ * roomGap
            val roomIdx   = rx + rz * MAP_SIZE
            val centerIdx = (rx + roomSize / 2 - 1) + (rz + roomSize / 2 + 1) * MAP_SIZE
            if (roomIdx >= colors.size || centerIdx >= colors.size) continue

            val roomCol   = colors[roomIdx]
            val centerCol = colors[centerIdx]
            if (roomCol == EMPTY) continue

            val room = tile.room ?: return
            RoomType.fromMapColor(roomCol)?.let { room.type = it }

            if (centerCol == roomCol && room.checkmark.equalsOneOf(MapCheckmark.UNDISCOVERED, MapCheckmark.QUESTION_MARK)) room.checkmark = MapCheckmark.NONE
            else MapCheckmark.fromMapColor(centerCol)?.let { room.checkmark = it }
        }
    }

    init {
        on<WorldEvent.Load> { unload() }
        onReceive<ClientboundMapItemDataPacket> { rescan() }
    }
}