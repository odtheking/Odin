package me.odinclient.dungeonmap.core.map

import me.odinclient.utils.render.Color

class Unknown(override val x: Int, override val z: Int) : Tile {
    override val color: Color = Color.TRANSPARENT
    override var state: RoomState = RoomState.UNDISCOVERED
}