package com.odtheking.odin.features.impl.dungeon.map

import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.Color.Companion.withAlpha
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.IVec2
import com.odtheking.odin.utils.skyblock.dungeon.DungeonScan
import com.odtheking.odin.utils.skyblock.dungeon.door.DoorRotation
import com.odtheking.odin.utils.skyblock.dungeon.door.DoorType
import com.odtheking.odin.utils.skyblock.dungeon.door.DungeonDoor
import com.odtheking.odin.utils.skyblock.dungeon.room.DungeonRoom
import com.odtheking.odin.utils.skyblock.dungeon.room.RoomRotation
import com.odtheking.odin.utils.skyblock.dungeon.room.RoomShape
import net.minecraft.client.gui.GuiGraphics

object DungeonMapModule : Module(
    "Map",
    description = "test"
) {

    private val hud by HUD("Map test", "test") {
        for (room in DungeonScan.rooms) {
            pose().pushMatrix()
            renderRoom(room)
            pose().popMatrix()
        }

        for ((_, door) in DungeonScan.doors) {
            pose().pushMatrix()
            renderDoor(door)
            pose().popMatrix()
        }
        (12 * 6) to (12 * 6)
    }

    // temporary rendering, easier to visual what's scanning nd stuff
    private fun GuiGraphics.renderRoom(room: DungeonRoom) {
        if (room.shape === RoomShape.UNKNOWN) return

        val color = when (room.shape) {
            RoomShape.TwoByTwo -> Colors.MINECRAFT_RED
            RoomShape.OneByOne -> Colors.MINECRAFT_GREEN
            RoomShape.TwoByOne -> Colors.MINECRAFT_BLUE
            RoomShape.FourByOne -> Colors.MINECRAFT_GOLD
            RoomShape.L -> Colors.MINECRAFT_LIGHT_PURPLE
            RoomShape.ThreeByOne -> Colors.MINECRAFT_AQUA
            else -> Colors.BLACK
        }.withAlpha(0.5f).rgba

        pose().translate(room.position.x * 12f, room.position.z * 12f)
        when (room.shape) {
            RoomShape.UNKNOWN -> {}

            RoomShape.OneByOne -> fill(0, 0, 10, 10, color)

            RoomShape.TwoByOne, RoomShape.ThreeByOne, RoomShape.FourByOne -> {
                var size = IVec2(10, (12 * room.shape.segmentAmount()) - 2)
                if (room.rotation === RoomRotation.East) size = size.flip()
                fill(0, 0, size.x, size.z, color)
            }

            RoomShape.TwoByTwo -> fill(0, 0, 22, 22, color)

            RoomShape.L -> {

            }
        }
    }

    private fun GuiGraphics.renderDoor(door: DungeonDoor) {
        val color = when (door.type) {
            DoorType.Normal -> Colors.WHITE
            DoorType.Wither -> Colors.BLACK
            DoorType.Blood -> Colors.MINECRAFT_RED
            DoorType.Fairy -> Colors.MINECRAFT_LIGHT_PURPLE
        }

        var offset = IVec2(1, 0)
        if (door.rotation === DoorRotation.Vertical) offset = offset.flip()

        pose().translate(
            (door.position.x * 12f) + (offset.x * 10) + (offset.z * 2),
            (door.position.z * 12f) + (offset.z * 10) + (offset.x * 2),
        )
        fill(
            0,
            0,
            2 + (4 * offset.z), 2 + (4 * offset.x), color.rgba)
    }
}