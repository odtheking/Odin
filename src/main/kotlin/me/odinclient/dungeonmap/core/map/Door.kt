package me.odinclient.dungeonmap.core.map

import me.odinclient.features.impl.dungeon.MapModule
import me.odinclient.utils.render.Color

class Door(override val x: Int, override val z: Int) : Tile {
    var type = DoorType.NONE
    var opened = false
    override var state: RoomState = RoomState.UNDISCOVERED
    override val color: Color
        get() = when (this.type) {
            DoorType.BLOOD -> MapModule.bloodDoorColor
            DoorType.ENTRANCE -> MapModule.entranceDoorColor
            DoorType.WITHER -> if (opened) MapModule.openWitherDoorColor else MapModule.witherDoorColor
            else -> MapModule.roomDoorColor
        }
}