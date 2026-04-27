package com.odtheking.odin.features.impl.dungeon.dungeonwaypoints

import com.odtheking.odin.config.WaypointPackFileUtils
import com.odtheking.odin.config.WaypointPackState
import com.odtheking.odin.config.normalized
import com.odtheking.odin.features.ModuleManager
import com.odtheking.odin.utils.modMessage
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import com.odtheking.odin.utils.skyblock.dungeon.map.tile.DungeonRoom
import net.minecraft.world.phys.AABB

suspend fun DungeonWaypoints.loadWaypoints() {
    val packState = ensurePackState()
    loadedPacks = packState.selectedPackIds.associateWithTo(mutableMapOf()) { packId -> copyWaypointMap(WaypointPackFileUtils.loadPack(packId)) }
    allActiveWaypoints = rebuildVisibleWaypoints()
    DungeonUtils.currentRoom?.setWaypoints()
}

suspend fun DungeonWaypoints.saveWaypoints() {
    ensurePackState()
    WaypointPackFileUtils.savePack(editPackId, copyWaypointMap(loadedPacks[editPackId] ?: mutableMapOf()))
}

internal suspend fun DungeonWaypoints.importEditableWaypoints(waypoints: MutableMap<String, MutableList<DungeonWaypoints.DungeonWaypoint>>) {
    ensurePackState()
    loadedPacks[editPackId] = copyWaypointMap(waypoints)
    saveWaypoints()
    allActiveWaypoints = rebuildVisibleWaypoints()
    DungeonUtils.currentRoom?.setWaypoints()
}

internal suspend fun DungeonWaypoints.importPack(packName: String, waypoints: MutableMap<String, MutableList<DungeonWaypoints.DungeonWaypoint>>): Boolean {
    if (!WaypointPackFileUtils.createPack(packName)) return false
    WaypointPackFileUtils.savePack(packName, copyWaypointMap(waypoints))
    ensurePackState(selectedPackIds + packName, packName)
    loadWaypoints()
    return true
}

internal suspend fun DungeonWaypoints.savePackSelection(selectedPackIds: List<String>, editPackId: String) {
    ensurePackState(selectedPackIds, editPackId)
    loadWaypoints()
}

internal suspend fun DungeonWaypoints.createPack(packName: String): Boolean {
    if (!WaypointPackFileUtils.createPack(packName)) return false
    ensurePackState(selectedPackIds + packName, packName)
    loadWaypoints()
    return true
}

internal suspend fun DungeonWaypoints.deletePack(packName: String): Boolean {
    if (WaypointPackFileUtils.getAllPacks().size <= 1) {
        modMessage("§cCannot delete the only pack!")
        return false
    }
    if (!WaypointPackFileUtils.deletePack(packName)) return false
    ensurePackState(selectedPackIds.filter { it != packName }, editPackId.takeIf { it != packName } ?: "")
    loadWaypoints()
    return true
}

internal suspend fun DungeonWaypoints.renamePack(oldName: String, newName: String): Boolean {
    if (!WaypointPackFileUtils.renamePack(oldName, newName)) return false
    val renamedSelection = selectedPackIds.map { if (it == oldName) newName else it }
    val renamedEditPack = if (editPackId == oldName) newName else editPackId
    ensurePackState(renamedSelection, renamedEditPack)
    loadWaypoints()
    return true
}

internal fun DungeonWaypoints.exportEditableWaypoints(): MutableMap<String, MutableList<DungeonWaypoints.DungeonWaypoint>> =
    copyWaypointMap(loadedPacks[editPackId] ?: mutableMapOf())

internal fun DungeonWaypoints.resetClickedWaypoints() {
    loadedPacks = loadedPacks.mapValuesTo(mutableMapOf()) { (_, packWaypoints) -> copyWaypointMap(packWaypoints) }
    allActiveWaypoints = rebuildVisibleWaypoints()
    DungeonUtils.currentRoom?.setWaypoints()
}

fun DungeonRoom.setWaypoints() {
    waypoints = DungeonWaypoints.allActiveWaypoints[data.name]
        ?.mapTo(mutableSetOf()) { waypoint ->
            waypoint.copy(blockPos = getRealCoords(waypoint.blockPos))
        } ?: mutableSetOf()
}

fun DungeonWaypoints.getWaypoints(room: DungeonRoom): MutableList<DungeonWaypoints.DungeonWaypoint> =
    allActiveWaypoints.getOrPut(room.data.name) { mutableListOf() }

fun DungeonWaypoints.getEditableWaypoints(room: DungeonRoom): MutableList<DungeonWaypoints.DungeonWaypoint> =
    loadedPacks.getOrPut(editPackId) { mutableMapOf() }.getOrPut(room.data.name) { mutableListOf() }

fun DungeonWaypoints.syncRoomToActive(room: DungeonRoom) {
    val mergedRoom = mergeRoomWaypoints(room.data.name)
    if (mergedRoom.isEmpty()) allActiveWaypoints.remove(room.data.name)
    else allActiveWaypoints[room.data.name] = mergedRoom
    room.setWaypoints()
}

private suspend fun DungeonWaypoints.ensurePackState(
    requestedSelection: List<String> = selectedPackIds,
    requestedEditPackId: String = editPackId,
): WaypointPackState {
    var availablePacks = WaypointPackFileUtils.getAllPacks()
    if (availablePacks.isEmpty()) {
        WaypointPackFileUtils.createPack("default")
        availablePacks = WaypointPackFileUtils.getAllPacks()
    }

    val normalizedState = WaypointPackState(
        selectedPackIds = requestedSelection.ifEmpty { selectedPackIds }.toMutableList(),
        editPackId = requestedEditPackId.ifBlank { editPackId },
    ).normalized(availablePacks.map { it.name })

    selectedPackIds = normalizedState.selectedPackIds.toMutableList()
    editPackId = normalizedState.editPackId
    ModuleManager.saveConfigurations()
    return normalizedState
}

private fun DungeonWaypoints.rebuildVisibleWaypoints(): MutableMap<String, MutableList<DungeonWaypoints.DungeonWaypoint>> {
    val roomNames = loadedPacks.values.flatMap { it.keys }.distinct()
    return roomNames.associateWithTo(mutableMapOf()) { roomName -> mergeRoomWaypoints(roomName) }.also { merged ->
        merged.entries.removeIf { it.value.isEmpty() }
    }
}

private fun DungeonWaypoints.mergeRoomWaypoints(roomName: String): MutableList<DungeonWaypoints.DungeonWaypoint> =
    selectedPackIds.fold(mutableListOf()) { merged, packId ->
        loadedPacks[packId]?.get(roomName)?.forEach { merged.add(it.resetRuntimeState()) }
        merged
    }

private fun copyWaypointMap(source: Map<String, List<DungeonWaypoints.DungeonWaypoint>>): MutableMap<String, MutableList<DungeonWaypoints.DungeonWaypoint>> =
    source.entries.associateTo(mutableMapOf()) { (room, waypoints) ->
        room to waypoints.mapTo(mutableListOf()) { it.resetRuntimeState() }
    }

private fun DungeonWaypoints.DungeonWaypoint.resetRuntimeState() = copy(
    color = color.copy(),
    aabb = AABB(aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ),
    isClicked = false,
)
