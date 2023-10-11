package me.odinclient.dungeonmap.core.map

import me.odinmain.utils.render.Color

interface Tile {
    val x: Int
    val z: Int
    var state: RoomState
    val color: Color
}