package me.odinclient.dungeonmap.core.map

import me.odinclient.OdinClient.Companion.config
import java.awt.Color

class Door(override val x: Int, override val z: Int) : Tile {
    var type = DoorType.NONE
    var opened = false
    override var state: RoomState = RoomState.UNDISCOVERED
    override val color: Color
        get() = when (this.type) {
            DoorType.BLOOD -> config.colorBloodDoor.toJavaColor()
            DoorType.ENTRANCE -> config.colorEntranceDoor.toJavaColor()
            DoorType.WITHER -> if (opened) config.colorOpenWitherDoor.toJavaColor() else config.colorWitherDoor.toJavaColor()
            else -> config.colorRoomDoor.toJavaColor()
        }
}