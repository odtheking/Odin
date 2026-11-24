package com.odtheking.odin.features.impl.dungeon.dungeonwaypoints

import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.clickgui.settings.impl.label
import com.odtheking.odin.features.impl.dungeon.dungeonwaypoints.DungeonWaypoints.DungeonWaypoint
import com.odtheking.odin.utils.Color
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.render.textDim
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import com.odtheking.odin.utils.toFixed
import net.minecraft.client.gui.GuiGraphicsExtractor

fun GuiGraphicsExtractor.drawWaypointEditorExample(): Pair<Int, Int> =
    drawEditorHud(
        title = "§fEditing Waypoints §8|§f Placing",
        text = "§fType: §5Normal§7, §r#${Colors.MINECRAFT_RED.hex()}§7, §3Outline§7, §cThrough Walls§7, §2Block Size",
        color = Colors.MINECRAFT_RED,
    )

fun GuiGraphicsExtractor.drawWaypointEditorHud() {
    if (!DungeonWaypoints.allowEdits) return

    val room = DungeonUtils.currentRoom ?: return
    val pos = reachPosition ?: return

    val hoveredWaypoint = room.waypoints.firstOrNull { it.blockPos == pos }
    drawEditorHud(
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
    append("§fType: §5${waypointType.label}")
    append("§7, §r#${color.hex()}§7")
    append(", ${if (filled) "§2Filled" else "§3Outline"}")
    append("§7, ${if (depthCheck) "§2Depth Check" else "§cThrough Walls"}")
    append("§7, ${if (useBlockSize) "§2Block Size" else "§3Size: ${sizeX.toFixed(2)}x${sizeY.toFixed(2)}x${sizeZ.toFixed(2)}"}")
}

private fun DungeonWaypoint.describe(): String = buildString {
    append("§fType: §5${type?.label ?: "None"}")
    append("§7, §r#${color.hex()}§7")
    title?.takeIf(String::isNotBlank)?.let { append(", §fTitle: §a$it§7") }
    append(", ${if (filled) "§2Filled" else "§3Outline"}")
    append("§7, ${if (depth) "§2Depth Check" else "§cThrough Walls"}")
    append("§7, §3Size: ${"${(aabb.maxX - aabb.minX).toFixed(2)}x${(aabb.maxY - aabb.minY).toFixed(2)}x${(aabb.maxZ - aabb.minZ).toFixed(2)}"}")
}