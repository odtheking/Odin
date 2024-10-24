package me.odinmain.utils.skyblock.dungeon.tiles

enum class Rotations(
    val x: Int,
    val z: Int
) {
    NORTH(15, 15),
    SOUTH(-15, -15),
    WEST(15, -15),
    EAST(-15, 15),
    NONE(0, 0);
}