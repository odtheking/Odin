package com.odtheking.odin.features.impl.dungeon.map

import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.Color.Companion.withAlpha
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.IVec2
import com.odtheking.odin.utils.render.text
import com.odtheking.odin.utils.skyblock.dungeon.DungeonScan
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import com.odtheking.odin.utils.skyblock.dungeon.door.DoorType
import com.odtheking.odin.utils.skyblock.dungeon.door.DungeonDoor
import com.odtheking.odin.utils.skyblock.dungeon.room.*
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.PlayerFaceRenderer

object DungeonMapModule : Module(
    "Map",
    description = "test"
) {
    val disableWorldScan by BooleanSetting(
        "Disable World Scan",
        false,
        desc = "Disables chunk/world scanning; builds rooms only from the map item."
    )

    private val hud by HUD("Map test", "test") {
        val matrices = pose()

        for (room in DungeonScan.rooms) {
            matrices.pushMatrix()
            renderRoom(room)
            matrices.popMatrix()
        }

        for ((_, door) in DungeonScan.doors) {
            matrices.pushMatrix()
            renderDoor(door)
            matrices.popMatrix()
        }
        
        for (player in DungeonUtils.dungeonTeammates) {
            if (player.isDead) continue

            val (posX, posZ) = player.mapScanRenderPosition()

            matrices.pushMatrix()
            matrices.translate(posX, posZ)

            player.locationSkin?.let { skin ->
                matrices.rotate(Math.toRadians(180.0 + player.mapRenderYaw()).toFloat())
                PlayerFaceRenderer.draw(this, skin, -5, -5, 10, false, false, -1)
            }

            matrices.popMatrix()
        }

        (12 * 6) to (12 * 6)
    }

    private fun GuiGraphics.renderRoom(room: DungeonRoom) {
        when (room) {
            is DungeonRoom.Collecting    -> return
            is DungeonRoom.MapResolved   -> render(room.position, room.shape, room.rotation, room.type, room.checkmark)
            is DungeonRoom.WorldResolved -> render(room.position, room.shape, room.rotation, room.type, room.checkmark)
        }
    }

    private fun GuiGraphics.render(
        position: IVec2,
        shape: RoomShape,
        rotation: RoomRotation?,
        type: RoomType,
        checkmark: MapCheckmark,
    ) {
        val color = when (type) {
            RoomType.CHAMPION -> Colors.MINECRAFT_YELLOW
            RoomType.BLOOD -> Colors.MINECRAFT_RED
            RoomType.FAIRY -> Colors.MINECRAFT_LIGHT_PURPLE
            RoomType.ENTRANCE -> Colors.MINECRAFT_DARK_GREEN
            RoomType.TRAP -> Colors.MINECRAFT_DARK_RED
            RoomType.NORMAL -> Colors.MINECRAFT_GOLD
            RoomType.PUZZLE -> Colors.MINECRAFT_DARK_PURPLE
            else -> Colors.MINECRAFT_GRAY
        }.withAlpha(0.5f).rgba

        pose().translate(position.x * 12f, position.z * 12f)
        when (shape) {
            RoomShape.OneByOne -> fill(0, 0, 10, 10, color)

            RoomShape.TwoByOne, RoomShape.ThreeByOne, RoomShape.FourByOne -> {
                var size = IVec2(10, (12 * shape.segments) - 2)
                if (rotation === RoomRotation.SOUTH) size = size.flip()
                fill(0, 0, size.x, size.z, color)
            }

            RoomShape.TwoByTwo -> fill(0, 0, 22, 22, color)

            RoomShape.L -> {
                if (rotation === RoomRotation.WEST) {
                    fill(0, 0, 10, 12, color)
                    fill(0, 12, 22, 22, color)
                } else if (rotation === RoomRotation.NORTH) {
                    fill(0, 0, 22, 10, color)
                    fill(12, 10, 22, 22, color)
                } else {
                    fill(0, 0, 22, 10, color)
                    fill(0, 10, 10, 22, color)
                }
            }
        }
        text(checkmark.symbol, 2, 1, Colors.WHITE, true)
    }

    private fun GuiGraphics.renderDoor(door: DungeonDoor) {
        val color = when (door.type) {
            DoorType.Normal -> Colors.WHITE
            DoorType.Wither -> Colors.BLACK
            DoorType.Blood -> Colors.MINECRAFT_RED
            DoorType.Fairy -> Colors.MINECRAFT_LIGHT_PURPLE
        }

        pose().translate(
            (door.position.x * 12f) + (door.rotation.offset.x * 10) + (door.rotation.offset.z * 2),
            (door.position.z * 12f) + (door.rotation.offset.z * 10) + (door.rotation.offset.x * 2),
        )
        fill(0, 0, 2 + (4 * door.rotation.offset.z), 2 + (4 * door.rotation.offset.x), color.rgba)
    }
}