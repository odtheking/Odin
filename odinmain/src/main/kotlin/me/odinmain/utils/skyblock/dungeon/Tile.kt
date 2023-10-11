package me.odinmain.utils.skyblock.dungeon


interface Tile {
    val x: Int
    val z: Int
    var state: RoomState
}