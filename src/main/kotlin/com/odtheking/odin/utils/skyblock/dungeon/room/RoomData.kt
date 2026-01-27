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
        private val coreToRoomData: HashMap<Int, RoomData> = kotlin.run {
            val roomData: ArrayList<RoomData> = JsonResourceLoader.loadJson("/assets/odin/rooms.json", arrayListOf())
            val map: HashMap<Int, RoomData> = hashMapOf()
            for (room in roomData) {
                for (core in room.cores) {
                    map[core] = room
                }
            }
            map
        }

        fun getRoomData(core: Int): RoomData? {
            return coreToRoomData[core]
        }
    }
}


enum class RoomType {
    ENTRANCE,
    FAIRY,
    NORMAL,
    BLOOD,
    CHAMPION,
    PUZZLE,
    TRAP,
    RARE;
}

enum class RoomShape {
    @SerializedName("Unknown")
    UNKNOWN,
    @SerializedName("L")
    L,
    @SerializedName("1x1")
    OneByOne,
    @SerializedName("1x2")
    TwoByOne,
    @SerializedName("1x3")
    ThreeByOne,
    @SerializedName("1x4")
    FourByOne,
    @SerializedName("2x2")
    TwoByTwo;

    fun segmentAmount(): Int {
        return when (this) {
            UNKNOWN -> 0
            OneByOne -> 1
            TwoByOne -> 2
            ThreeByOne, L -> 3
            FourByOne, TwoByTwo -> 4
        }
    }
}

enum class RoomRotation {
    North,
    East,
    South,
    West;
}