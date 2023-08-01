package me.odinclient.dungeonmap.core.map

import me.odinclient.dungeonmap.core.RoomData
import me.odinclient.features.impl.dungeon.MapModule
import java.awt.Color

class Room(override val x: Int, override val z: Int, var data: RoomData) : Tile {
    var core = 0
    var hasMimic = false
    var isSeparator = false
    override var state: RoomState = RoomState.UNDISCOVERED
    override val color: Color
        get() = when (data.type) {
            RoomType.BLOOD -> MapModule.bloodColor.javaColor
            RoomType.CHAMPION -> MapModule.miniBossColor.javaColor
            RoomType.ENTRANCE -> MapModule.entranceColor.javaColor
            RoomType.FAIRY -> MapModule.fairyColor.javaColor
            RoomType.PUZZLE -> MapModule.puzzleColor.javaColor
            RoomType.RARE -> MapModule.rareColor.javaColor
            RoomType.TRAP -> MapModule.trapColor.javaColor
            else -> if (hasMimic) MapModule.mimicRoomColor.javaColor else MapModule.roomColor.javaColor
        }
}