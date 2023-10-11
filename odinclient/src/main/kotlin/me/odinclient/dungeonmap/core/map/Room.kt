package me.odinclient.dungeonmap.core.map

import me.odinclient.dungeonmap.core.RoomData
import me.odinclient.features.impl.dungeon.MapModule
import me.odinmain.utils.render.Color

class Room(override val x: Int, override val z: Int, var data: RoomData) : Tile {
    var core = 0
    var hasMimic = false
    var isSeparator = false
    override var state: RoomState = RoomState.UNDISCOVERED
    override val color: Color
        get() = when (data.type) {
            RoomType.BLOOD -> MapModule.bloodColor
            RoomType.CHAMPION -> MapModule.miniBossColor
            RoomType.ENTRANCE -> MapModule.entranceColor
            RoomType.FAIRY -> MapModule.fairyColor
            RoomType.PUZZLE -> MapModule.puzzleColor
            RoomType.RARE -> MapModule.rareColor
            RoomType.TRAP -> MapModule.trapColor
            else -> if (hasMimic) MapModule.mimicRoomColor else MapModule.roomColor
        }
}