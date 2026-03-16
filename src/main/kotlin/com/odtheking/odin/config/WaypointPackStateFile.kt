package com.odtheking.odin.config

data class WaypointPackState(
    val selectedPackIds: MutableList<String> = mutableListOf(),
    var editPackId: String = "",
)

fun WaypointPackState.normalized(availablePackIds: List<String>): WaypointPackState {
    if (availablePackIds.isEmpty()) return WaypointPackState()

    val selectedPackIds = selectedPackIds
        .map(String::trim)
        .filter { it.isNotBlank() && it in availablePackIds }
        .distinct()
        .toMutableList()

    val editPackId = editPackId
        .takeIf { it in availablePackIds }
        ?: selectedPackIds.firstOrNull()
        ?: availablePackIds.first()

    if (selectedPackIds.isEmpty()) selectedPackIds.add(editPackId)
    if (editPackId !in selectedPackIds) selectedPackIds.add(editPackId)

    return WaypointPackState(selectedPackIds, editPackId)
}