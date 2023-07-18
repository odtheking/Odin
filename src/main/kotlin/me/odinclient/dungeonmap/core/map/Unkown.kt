package me.odinclient.dungeonmap.core.map

import java.awt.Color

class Unknown(override val x: Int, override val z: Int) : Tile {
    override val color: Color = Color(0, 0, 0, 0)
    override var state: RoomState = RoomState.UNDISCOVERED
}