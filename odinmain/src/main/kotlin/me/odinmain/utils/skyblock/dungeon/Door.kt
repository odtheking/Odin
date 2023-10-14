package me.odinmain.utils.skyblock.dungeon

import me.odinmain.OdinMain
import me.odinmain.utils.render.Color

class Door(override val x: Int, override val z: Int) : Tile {
    var type = DoorType.NONE
    var opened = false
    override var state: RoomState = RoomState.UNDISCOVERED
    override val color: Color
        get() = when (this.type) {
            DoorType.BLOOD -> OdinMain.MapColors.bloodDoorColor
            DoorType.ENTRANCE -> OdinMain.MapColors.entranceDoorColor
            DoorType.WITHER -> if (opened) OdinMain.MapColors.openWitherDoorColor else OdinMain.MapColors.witherDoorColor
            else -> OdinMain.MapColors.roomDoorColor
        }
}