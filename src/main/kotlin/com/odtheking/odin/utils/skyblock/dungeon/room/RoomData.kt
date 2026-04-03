package com.odtheking.odin.utils.skyblock.dungeon.room

import com.google.gson.annotations.SerializedName
import com.odtheking.odin.utils.JsonResourceLoader

@ConsistentCopyVisibility
data class RoomData private constructor(
    val name: String,
    val type: RoomType,
    val shape: RoomShape,
    val cores: List<Int>,
    val crypts: Int,
    val secrets: Int,
    val trappedChests: Int,
) {
    companion object {
        private val coreToRoomData: HashMap<Int, RoomData> = run {
            val roomData: ArrayList<RoomData> = JsonResourceLoader.loadJson("/assets/odin/rooms.json", arrayListOf())
            val map: HashMap<Int, RoomData> = hashMapOf()
            for (room in roomData) {
                for (core in room.cores) {
                    map[core] = room
                }
            }
            map
        }

        fun getRoomData(core: Int): RoomData? = coreToRoomData[core]
    }
}

enum class RoomType(val mapColor: Byte) {
    ENTRANCE(30),
    FAIRY(82),
    NORMAL(63),
    BLOOD(18),
    CHAMPION(74),
    UNKNOWN(85),
    PUZZLE(66),
    TRAP(62),
    UNDISCOVERED(-1);

    companion object {
        private val BY_COLOR = entries.associateBy { it.mapColor }
        fun fromMapColor(color: Byte) = BY_COLOR[color]
    }
}

enum class RoomShape(val segments: Int) {
    @SerializedName("L")
    L(3),
    @SerializedName("1x1")
    OneByOne(1),
    @SerializedName("1x2")
    TwoByOne(2),
    @SerializedName("1x3")
    ThreeByOne(3),
    @SerializedName("1x4")
    FourByOne(4),
    @SerializedName("2x2")
    TwoByTwo(4);
}

enum class RoomRotation {
    WEST,
    SOUTH,
    NORTH,
    EAST;
}