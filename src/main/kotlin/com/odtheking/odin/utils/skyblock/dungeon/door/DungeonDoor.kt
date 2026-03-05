package com.odtheking.odin.utils.skyblock.dungeon.door

import com.odtheking.odin.utils.IVec2

class DungeonDoor(
    val position: IVec2,
    val rotation: DoorRotation,
    var type: DoorType,
)

enum class DoorType {
    Normal,
    Wither,
    Blood,
    Fairy,
}

enum class DoorRotation(val offset: IVec2) {
    Horizontal(IVec2(1, 0)),
    Vertical(IVec2(0, 1)),
}