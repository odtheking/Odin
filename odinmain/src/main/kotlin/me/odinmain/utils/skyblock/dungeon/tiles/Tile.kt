package me.odinmain.utils.skyblock.dungeon.tiles

import me.odinmain.utils.render.Color


interface Tile {
    val x: Int
    val z: Int
    var state: RoomState
}