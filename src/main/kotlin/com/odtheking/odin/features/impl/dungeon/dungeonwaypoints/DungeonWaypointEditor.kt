package com.odtheking.odin.features.impl.dungeon.dungeonwaypoints

import com.odtheking.odin.OdinMod
import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.events.InputEvent
import com.odtheking.odin.events.RenderEvent
import com.odtheking.odin.features.impl.dungeon.dungeonwaypoints.DungeonWaypoints.DungeonWaypoint
import com.odtheking.odin.features.impl.dungeon.dungeonwaypoints.DungeonWaypoints.WaypointType
import com.odtheking.odin.features.impl.render.Etherwarp
import com.odtheking.odin.utils.Color.Companion.withAlpha
import com.odtheking.odin.utils.devMessage
import com.odtheking.odin.utils.getBlockBounds
import com.odtheking.odin.utils.isEtherwarpItem
import com.odtheking.odin.utils.modMessage
import com.odtheking.odin.utils.render.drawBoxes
import com.odtheking.odin.utils.render.drawStyledBox
import com.odtheking.odin.utils.render.drawText
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import com.odtheking.odin.utils.skyblock.dungeon.map.tile.DungeonRoom
import kotlinx.coroutines.launch
import net.minecraft.core.BlockPos
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.HitResult
import org.lwjgl.glfw.GLFW

internal fun DungeonWaypoints.renderWaypoints(event: RenderEvent.Extract) {
    if (!DungeonUtils.inClear) return
    val room = DungeonUtils.currentRoom ?: return
    event.drawBoxes(room.waypoints, disableDepth)
    room.waypoints.forEach { waypoint ->
        if (waypoint.isClicked || waypoint.title == null) return@forEach
        event.drawText(
            waypoint.title, waypoint.blockPos.center.add(0.0, 0.1 * titleScale, 0.0),
            titleScale, waypoint.depth
        )
    }

    reachPosition?.takeIf { allowEdits }?.let { pos ->
        event.drawStyledBox(relativeAabbAt(pos).move(pos), color.withAlpha(0.3f), style = if (filled) 0 else 1, depthCheck)
    }
}

internal fun DungeonWaypoints.handleEditorInput(event: InputEvent) {
    if (event.key.value != GLFW.GLFW_MOUSE_BUTTON_RIGHT || mc.screen != null) return
    cacheEtherwarpTarget()
    if (!allowEdits) return
    val room = DungeonUtils.currentRoom ?: return
    val pos = reachPosition ?: return
    val blockPos = room.getRelativeCoords(pos)
    val visibleWaypoint = room.waypoints.firstOrNull { it.blockPos == pos }
    val editableWaypoints = getEditableWaypoints(room)
    val editableWaypoint = editableWaypoints.firstOrNull { it.blockPos == blockPos }
    if (visibleWaypoint != null && editableWaypoint == null) {
        modMessage("§eThat waypoint belongs to another active pack. Switch edit packs to change it.")
        return
    }
    if (allowTextEdit && mc.player?.isCrouching == true) {
        openWaypointTitlePrompt(room, blockPos, relativeAabbAt(pos), editableWaypoints)
        return
    }
    if (editableWaypoints.removeIf { it.blockPos == blockPos }) {
        devMessage("Removed waypoint at $blockPos")
        syncRoomToActive(room)
        OdinMod.scope.launch { saveWaypoints() }
        return
    }
    editableWaypoints.add(createWaypoint(blockPos, relativeAabbAt(pos)))
    devMessage("Added waypoint at $blockPos")
    syncRoomToActive(room)
    OdinMod.scope.launch { saveWaypoints() }
}

internal val reachPosition: BlockPos?
    get() {
        val hitResult = mc.hitResult
        return when {
            hitResult?.type == HitResult.Type.MISS -> Etherwarp.getEtherPos(mc.player?.position(), 5.0, returnEnd = true).pos
            hitResult is BlockHitResult -> hitResult.blockPos
            else -> null
        }
    }

private fun DungeonWaypoints.cacheEtherwarpTarget() {
    mc.player?.mainHandItem?.isEtherwarpItem()?.let { item ->
        Etherwarp.getEtherPos(mc.player?.position(), 56.0 + item.getInt("tuned_transmission").orElse(0))
            .takeIf { it.succeeded && it.pos != null }
            ?.also {
                lastEtherTime = System.currentTimeMillis()
                lastEtherPos = it.pos
            }
    }
}

private fun DungeonWaypoints.openWaypointTitlePrompt(
    room: DungeonRoom,
    blockPos: BlockPos,
    aabb: AABB,
    editableWaypoints: MutableList<DungeonWaypoint>,
) {
    mc.setScreen(TextPromptScreen("Waypoint Name").setCallback { text ->
        editableWaypoints.removeIf { it.blockPos == blockPos }
        editableWaypoints.add(createWaypoint(blockPos, aabb, text))
        devMessage("Added waypoint with $text at $blockPos")
        syncRoomToActive(room)
        mc.setScreen(null)
        OdinMod.scope.launch { saveWaypoints() }
    })
}

private fun DungeonWaypoints.createWaypoint(blockPos: BlockPos, aabb: AABB, title: String? = null) = DungeonWaypoint(
    blockPos = blockPos,
    color = color.copy(),
    filled = filled,
    depth = depthCheck,
    aabb = aabb,
    title = title,
    type = WaypointType.getByInt(waypointType),
)

internal fun DungeonWaypoints.relativeAabbAt(pos: BlockPos): AABB =
    if (!useBlockSize) AABB(BlockPos.ZERO).inflate((sizeX - 1.0) / 2.0, (sizeY - 1.0) / 2.0, (sizeZ - 1.0) / 2.0)
    else pos.getBlockBounds() ?: AABB(BlockPos.ZERO)