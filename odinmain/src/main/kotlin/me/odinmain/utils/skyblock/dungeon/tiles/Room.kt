package me.odinmain.utils.skyblock.dungeon.tiles

import me.odinmain.utils.Vec2

class Room(override val x: Int, override val z: Int, var data: RoomData) : Tile {
    var vec2 = Vec2(x, z)
    var core = 0
    var rotation = Rotations.NONE
    override var state: RoomState = RoomState.UNDISCOVERED
}