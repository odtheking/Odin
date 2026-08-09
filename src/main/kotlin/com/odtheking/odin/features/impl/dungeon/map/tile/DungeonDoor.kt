package com.odtheking.odin.features.impl.dungeon.map.tile

import com.odtheking.odin.utils.Color
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.IVec2

class DungeonDoor(
    val position: IVec2,
    val rotation: DoorRotation,
    var type: DoorType,
    var color: Color = Colors.WHITE,
) {
    val originTileIndex = position.x + position.z * 6
    val destinationTileIndex = position.x + rotation.offset.x + (position.z + rotation.offset.z) * 6

    val worldX = (position.x - 6) * 32 + 7 + rotation.offset.x * 16
    val worldZ = (position.z - 6) * 32 + 7 + rotation.offset.z * 16
}

enum class DoorType {
    Normal,
    Wither,
    Blood,
    Fairy;

    companion object {
        fun fromColor(color: Byte): DoorType {
            return when (color) {
                119.toByte() -> Wither
                RoomType.BLOOD.mapColor -> Blood
                RoomType.FAIRY.mapColor -> Fairy
                else -> Normal
            }
        }
    }
}

enum class DoorRotation(val offset: IVec2) {
    Horizontal(IVec2(1, 0)),
    Vertical(IVec2(0, 1)),
}