package me.odinmain.utils.skyblock.dungeon

import me.odinmain.utils.render.Color


interface Tile {
    val x: Int
    val z: Int
    var state: RoomState
    val color: Color
}