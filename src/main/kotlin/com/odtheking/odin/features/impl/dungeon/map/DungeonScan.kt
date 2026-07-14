package com.odtheking.odin.features.impl.dungeon.map

import com.odtheking.odin.events.FloorEnterEvent
import com.odtheking.odin.events.LevelEvent
import com.odtheking.odin.events.MapUpdateEvent
import com.odtheking.odin.events.RoomEnterEvent
import com.odtheking.odin.events.core.EventBus
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.impl.dungeon.map.tile.*
import com.odtheking.odin.utils.Color
import com.odtheking.odin.utils.IVec2
import com.odtheking.odin.utils.skyblock.dungeon.Floor
import net.minecraft.world.entity.player.Player
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

object DungeonScan {

    const val ROOM_SPACING = 4

    var roomSize = 16
    var roomGap = 20
    var startX = 5
    var startY = 5

    val tiles: Array<DungeonTile> = Array(36) { index -> DungeonTile(position = IVec2(x = index % 6, z = index / 6)) }
    val rooms: CopyOnWriteArrayList<DungeonRoom> = CopyOnWriteArrayList()
    val doors: ConcurrentHashMap<IVec2, DungeonDoor> = ConcurrentHashMap()

    val viewableDoors = CopyOnWriteArrayList<Pair<DungeonDoor, Color>>()
    val pathHints = CopyOnWriteArrayList<DungeonTile>()

    val directions = arrayOf(1 to 0, -1 to 0, 0 to 1, 0 to -1)

    init {
        on<LevelEvent.Load> { reset() }
        on<FloorEnterEvent> { initClient(floor) }

        on<RoomEnterEvent> { room?.let { updateViewableDoors() } }
        on<MapUpdateEvent> { updateViewableDoors() }

        listOf(WorldScan, MapScan, SpecialColumn).forEach { EventBus.subscribe(it) }
    }

    private fun reset() {
        repeat(36) { index -> tiles[index] = DungeonTile(position = IVec2(x = index % 6, z = index / 6)) }
        rooms.clear()
        doors.clear()
        viewableDoors.clear()
        pathHints.clear()
        roomSize = 16
        roomGap = 20
        startX = 5
        startY = 5
    }

    fun initClient(floor: Floor) {
        roomSize = if (floor.floorNumber <= 3) 18 else 16
        roomGap = roomSize + ROOM_SPACING

        startX = when {
            floor.floorNumber <= 1 -> 22
            floor.floorNumber <= 3 -> 11
            else -> 5
        }

        startY = when (floor.floorNumber) {
            0 -> 22
            4 -> 16
            in 1..3 -> 11
            else -> 5
        }
    }

    fun playerRenderPosition(entity: Player?, mapPos: IVec2): Pair<Float, Float> {
        entity?.let {
            val mapX = (it.x.toFloat() + 200f) * roomGap / 32f
            val mapZ = (it.z.toFloat() + 200f) * roomGap / 32f
            return mapX to mapZ
        }

        val pixelX = (mapPos.x + 128) / 2f - startX
        val pixelY = (mapPos.z + 128) / 2f - startY
        return pixelX to pixelY
    }

    fun updateViewableDoors() {
        pathHints.clear()
        viewableDoors.clear()
        for ((_, door) in doors) {
            val originTile = tiles[door.originTileIndex]
            val destTile = tiles[door.destinationTileIndex]
            val originView = originTile.room?.isViewable == true
            val destView = destTile.room?.isViewable == true
            if (!originView && !destView) continue

            (if (originTile.room?.isViewable != true) originTile else if (destTile.room?.isViewable != true) destTile else null)?.let {
                pathHints.add(it)
            }

            viewableDoors.add(door to when (door.type) {
                DoorType.Wither -> DungeonMap.witherDoorColor
                DoorType.Blood -> DungeonMap.bloodDoorColor
                DoorType.Fairy -> DungeonMap.fairyDoorColor
                else -> {
                    if (!originView || !destView) DungeonMap.unknownDoorColor
                    else listOfNotNull(originTile.room, destTile.room)
                        .firstOrNull { it.type != RoomType.NORMAL && it.type != RoomType.FAIRY }
                        ?.type?.let { room -> roomTypeColor(room) } ?: DungeonMap.normalDoorColor
                }
            })
        }
    }
}