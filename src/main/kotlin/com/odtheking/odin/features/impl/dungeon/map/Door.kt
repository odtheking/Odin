package com.odtheking.odin.features.impl.dungeon.map

import com.odtheking.odin.features.impl.dungeon.DungeonMap
import com.odtheking.odin.utils.Color
import com.odtheking.odin.utils.Color.Companion.darker
import com.odtheking.odin.utils.equalsOneOf
import com.odtheking.odin.utils.skyblock.dungeon.tiles.RoomState
import com.odtheking.odin.utils.skyblock.dungeon.tiles.RoomType
import net.minecraft.client.gui.GuiGraphics

class Door (val pos: Vec2i, var type: Type, val rooms: MutableList<MapRoom.Tile>) {

    init {
        rooms.forEach { it.owner?.doors?.add(this) }
    }

    enum class Type { BLOOD, NORMAL, WITHER }

    var locked = type.equalsOneOf(Type.WITHER, Type.BLOOD)
    val seen get() = rooms.any { it.owner?.state !in setOf(RoomState.UNDISCOVERED, RoomState.UNOPENED) }

    private fun size(): Vec2i {
        val xOffset = ((pos.x + 185) shr 4) % 2
        val zOffset = ((pos.z + 185) shr 4) % 2
        return Vec2i(
            (xOffset xor 1) * DungeonMap.doorThickness + xOffset * 4,
            (zOffset xor 1) * DungeonMap.doorThickness + zOffset * 4
        )
    }

    inline val placement: Vec2i
        get() {
            val x = (pos.x + 185) shr 4
            val z = (pos.z + 185) shr 4
            val xEven = x % 2
            val zEven = z % 2
            val thicknessBasedOffset = (16 - DungeonMap.doorThickness) / 2
            val xOffset = (x shr 1) * 20 + xEven * 16 + (xEven xor 1) * thicknessBasedOffset
            val yOffset = (z shr 1) * 20 + zEven * 16 + (zEven xor 1) * thicknessBasedOffset

            return Vec2i(xOffset, yOffset)
        }

    fun render(graphics: GuiGraphics) {
        val size = size()
        if (size != Vec2i(0, 0)) {
            val matrices = graphics.pose()
            matrices.pushMatrix()
            matrices.translate(placement.x.toFloat(), placement.z.toFloat())

            graphics.fill(0, 0, size.x, size.z, color().rgba)

            matrices.popMatrix()
        }
    }

    private fun color(): Color {
        val hasUnopenedRoom = rooms.any { it.owner?.state in setOf(RoomState.UNDISCOVERED, RoomState.UNOPENED) }

        return when {
            hasUnopenedRoom && type != Type.NORMAL -> getLockedDoorColor()
            hasUnopenedRoom -> DungeonMap.unopenedDoorColor
            else -> getOpenDoorColor()
        }
    }

    private fun getLockedDoorColor(): Color = when (type) {
        Type.BLOOD -> if (locked) DungeonMap.bloodDoorColor.darker(DungeonMap.darkenMultiplier) else DungeonMap.bloodDoorColor
        Type.WITHER -> if (locked) DungeonMap.witherDoorColor.darker(DungeonMap.darkenMultiplier) else DungeonMap.witherDoorColor
        Type.NORMAL -> DungeonMap.unopenedDoorColor
    }

    private fun getOpenDoorColor(): Color = when (type) {
        Type.BLOOD -> DungeonMap.bloodDoorColor
        Type.WITHER -> if (locked) DungeonMap.witherDoorColor
                      else rooms.firstOrNull { it.owner?.data?.type == RoomType.FAIRY }?.let { DungeonMap.fairyDoorColor }
                           ?: DungeonMap.normalDoorColor
        Type.NORMAL -> getDoorColorByRoomType()
    }

    private fun getDoorColorByRoomType(): Color {
        val specialRoom = rooms.firstOrNull { it.owner?.data?.type !in setOf(RoomType.NORMAL, RoomType.FAIRY) }
        return when (specialRoom?.owner?.data?.type) {
            RoomType.ENTRANCE -> DungeonMap.entranceDoorColor
            RoomType.BLOOD -> DungeonMap.bloodDoorColor
            RoomType.CHAMPION -> DungeonMap.championDoorColor
            RoomType.PUZZLE -> DungeonMap.puzzleDoorColor
            RoomType.RARE -> DungeonMap.rareDoorColor
            RoomType.TRAP -> DungeonMap.trapDoorColor
            else -> DungeonMap.normalDoorColor
        }
    }
}