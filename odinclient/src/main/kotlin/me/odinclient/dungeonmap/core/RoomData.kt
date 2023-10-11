package me.odinclient.dungeonmap.core

import me.odinclient.dungeonmap.core.map.RoomType

data class RoomData(
    val name: String,
    val type: RoomType,
    val cores: List<Int>,
    val crypts: Int,
    val secrets: Int,
    val trappedChests: Int,
)