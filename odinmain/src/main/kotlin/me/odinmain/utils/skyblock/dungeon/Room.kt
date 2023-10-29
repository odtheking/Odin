package me.odinmain.utils.skyblock.dungeon

import me.odinmain.OdinMain
import me.odinmain.utils.render.Color
import net.minecraft.util.EnumFacing

class Room(override val x: Int, override val z: Int, var data: RoomData) : Tile {
    var core = 0
    var hasMimic = false
    var isSeparator = false
    var rotation = EnumFacing.NORTH
    override var state: RoomState = RoomState.UNDISCOVERED
    override val color: Color
        get() = when (data.type) {
            RoomType.BLOOD -> OdinMain.MapColors.bloodColor
            RoomType.CHAMPION -> OdinMain.MapColors.miniBossColor
            RoomType.ENTRANCE -> OdinMain.MapColors.entranceColor
            RoomType.FAIRY -> OdinMain.MapColors.fairyColor
            RoomType.PUZZLE -> OdinMain.MapColors.puzzleColor
            RoomType.RARE -> OdinMain.MapColors.rareColor
            RoomType.TRAP -> OdinMain.MapColors.trapColor
            else -> if (hasMimic) OdinMain.MapColors.mimicRoomColor else OdinMain.MapColors.roomColor
        }
}