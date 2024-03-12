package me.odinmain.utils.skyblock.dungeon

import me.odinmain.OdinMain.MapColors
import me.odinmain.utils.render.Color
import net.minecraft.util.EnumFacing

class Room(override val x: Int, override val z: Int, var data: RoomData) : Tile {
    var core = 0
    var hasMimic = false
    var isSeparator = false
    var rotation = Rotations.NONE
    var rotationCore: Int? = null
    override var state: RoomState = RoomState.UNDISCOVERED
    override val color: Color
        get() = when (data.type) {
            RoomType.BLOOD -> MapColors.bloodColor
            RoomType.CHAMPION -> MapColors.miniBossColor
            RoomType.ENTRANCE -> MapColors.entranceColor
            RoomType.FAIRY -> MapColors.fairyColor
            RoomType.PUZZLE -> MapColors.puzzleColor
            RoomType.RARE -> MapColors.rareColor
            RoomType.TRAP -> MapColors.trapColor
            else -> if (hasMimic) MapColors.mimicRoomColor else MapColors.roomColor
        }
}