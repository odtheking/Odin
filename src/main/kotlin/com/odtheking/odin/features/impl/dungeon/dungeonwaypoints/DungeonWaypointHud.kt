package com.odtheking.odin.features.impl.dungeon.dungeonwaypoints

import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.features.impl.dungeon.dungeonwaypoints.DungeonWaypoints.DungeonWaypoint
import com.odtheking.odin.utils.Color
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.render.textDim
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import com.odtheking.odin.utils.toFixed
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.world.phys.AABB

internal fun GuiGraphicsExtractor.drawWaypointEditorHud(example: Boolean): Pair<Int, Int> {
    if (example) {
        return drawEditorHud(
            title = "§fEditing Waypoints §8|§f Placing",
            text = "§fType: §5Normal§7, §r#${Colors.MINECRAFT_RED.hex()}§7, §3Outline§7, §cThrough Walls§7, §2Block Size",
            color = Colors.MINECRAFT_RED,
        )
    }

    if (!DungeonWaypoints.allowEdits) return 0 to 0

    val room = DungeonUtils.currentRoom
    val pos = reachPosition
    if (room == null || pos == null) return 0 to 0

    val hoveredWaypoint = room.waypoints.firstOrNull { it.blockPos == pos }
    return drawEditorHud(
        title = "§fEditing Waypoints §8|§f ${if (hoveredWaypoint == null) "Placing" else "Viewing"}",
        text = hoveredWaypoint?.describe() ?: DungeonWaypoints.describeNextWaypoint(),
        color = hoveredWaypoint?.color ?: DungeonWaypoints.color,
    )
}

private fun GuiGraphicsExtractor.drawEditorHud(title: String, text: String, color: Color): Pair<Int, Int> {
    val textWidth = textDim(text, 0, 10, color).first

    centeredText(mc.font, title, textWidth / 2, 0, Colors.WHITE.rgba)
    return textWidth to 19
}

private fun DungeonWaypoints.describeNextWaypoint(): String = buildString {
    append("§fType: §5${DungeonWaypoints.WaypointType.getByInt(waypointType)?.displayName ?: "None"}")
    append("§7, §r#${color.hex()}§7")
    append(", ${if (filled) "§2Filled" else "§3Outline"}")
    append("§7, ${if (depthCheck) "§2Depth Check" else "§cThrough Walls"}")
    append("§7, ${if (useBlockSize) "§2Block Size" else "§3Size: ${sizeX.toFixed(2)}x${sizeY.toFixed(2)}x${sizeZ.toFixed(2)}"}")
}

private fun DungeonWaypoint.describe(): String = buildString {
    append("§fType: §5${type?.displayName ?: "None"}")
    append("§7, §r#${color.hex()}§7")
    title?.takeIf(String::isNotBlank)?.let { append(", §fTitle: §a$it§7") }
    append(", ${if (filled) "§2Filled" else "§3Outline"}")
    append("§7, ${if (depth) "§2Depth Check" else "§cThrough Walls"}")
    append("§7, §3Size: ${aabb.xSizeText()}")
}

private fun AABB.xSizeText(): String =
    "${(maxX - minX).toFixed(2)}x${(maxY - minY).toFixed(2)}x${(maxZ - minZ).toFixed(2)}"