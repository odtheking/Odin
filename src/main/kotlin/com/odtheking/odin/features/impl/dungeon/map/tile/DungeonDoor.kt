package com.odtheking.odin.features.impl.dungeon.map.tile

import com.odtheking.odin.utils.IVec2

class DungeonDoor(
    val position: IVec2,
    val rotation: DoorRotation,
    var type: DoorType
) {
    val originTileIndex by lazy { position.x + position.z * 6 }
    val destinationTileIndex by lazy { position.x + rotation.offset.x + (position.z + rotation.offset.z) * 6 }
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