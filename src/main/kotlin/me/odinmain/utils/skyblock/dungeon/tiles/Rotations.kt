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



    fun rotateY(): Rotations {
        return when (this) {
            NORTH -> EAST
            EAST -> SOUTH
            SOUTH -> WEST
            WEST -> NORTH
            else -> throw IllegalStateException("Unable to get Y-rotated facing of $this")
        }
    }

    fun rotateYCCW(): Rotations {
        return when (this) {
            NORTH -> WEST
            EAST -> NORTH
            SOUTH -> EAST
            WEST -> SOUTH
            else -> throw IllegalStateException("Unable to get CCW facing of $this")
        }
    }

    fun opposite(): Rotations {
        return when (this) {
            NORTH -> SOUTH
            SOUTH -> NORTH
            WEST -> EAST
            EAST -> WEST
            NONE -> NONE
        }
    }
}

