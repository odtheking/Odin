package me.odinmain.utils.skyblock.dungeon

data class RoomData(
    val name: String,
    val type: RoomType,
    val cores: List<Int>,
    val crypts: Int,
    val secrets: Int,
    val trappedChests: Int,
)
