package me.odinclient.dungeonmap.core.map

import me.odinclient.OdinClient.Companion.config
import me.odinclient.dungeonmap.core.RoomData
import java.awt.Color

class Room(override val x: Int, override val z: Int, var data: RoomData) : Tile {
    var core = 0
    var hasMimic = false
    var isSeparator = false
    override var state: RoomState = RoomState.UNDISCOVERED
    override val color: Color
        get() = when (data.type) {
            RoomType.BLOOD -> config.colorBlood.toJavaColor()
            RoomType.CHAMPION -> config.colorMiniboss.toJavaColor()
            RoomType.ENTRANCE -> config.colorEntrance.toJavaColor()
            RoomType.FAIRY -> config.colorFairy.toJavaColor()
            RoomType.PUZZLE -> config.colorPuzzle.toJavaColor()
            RoomType.RARE -> config.colorRare.toJavaColor()
            RoomType.TRAP -> config.colorTrap.toJavaColor()
            else -> if (hasMimic) config.colorRoomMimic.toJavaColor() else config.colorRoom.toJavaColor()
        }
}