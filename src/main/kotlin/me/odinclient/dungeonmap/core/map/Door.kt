package me.odinclient.dungeonmap.core.map

import me.odinclient.OdinClient.Companion.config
import me.odinclient.features.impl.dungeon.MapModule
import java.awt.Color

class Door(override val x: Int, override val z: Int) : Tile {
    var type = DoorType.NONE
    var opened = false
    override var state: RoomState = RoomState.UNDISCOVERED
    override val color: Color
        get() = when (this.type) {
            DoorType.BLOOD -> MapModule.bloodDoorColor.javaColor
            DoorType.ENTRANCE -> MapModule.entranceDoorColor.javaColor
            DoorType.WITHER -> if (opened) MapModule.openWitherDoorColor.javaColor else MapModule.witherDoorColor.javaColor
            else -> MapModule.roomDoorColor.javaColor
        }
}