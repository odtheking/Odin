package com.odtheking.odin.features.impl.render.waypoints

import com.odtheking.odin.utils.noControlCodes
import com.odtheking.odin.utils.skyblock.Island
import com.odtheking.odin.utils.skyblock.KuudraUtils
import com.odtheking.odin.utils.skyblock.LocationUtils
import com.odtheking.odin.utils.skyblock.SplitsManager
import com.odtheking.odin.utils.skyblock.dungeon.DungeonListener
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import com.odtheking.odin.utils.skyblock.dungeon.M7Phases
import java.util.*

enum class AreaGroup(val title: String) { DUNGEON("Dungeon"), KUUDRA("Kuudra"), ISLANDS("Islands") }

class WaypointArea(val key: String, val label: String, val group: AreaGroup)

object WaypointAreas {

    private val bossPhases = mapOf(
        6 to listOf("Terracottas", "Giants", "Cleared"),
        7 to listOf("Maxor", "Storm", "Terminals", "Goldor", "Necron", "Cleared"),
    )

    val all: List<WaypointArea> = buildList {
        for (floor in 1..5) add(WaypointArea("FLOOR_$floor", "F$floor / M$floor Boss", AreaGroup.DUNGEON))
        for ((floor, phases) in bossPhases) for (phase in phases) add(WaypointArea("F${floor}_${phase.uppercase()}", "F$floor $phase", AreaGroup.DUNGEON))
        for (tier in 1..5) add(WaypointArea("KUUDRA_T$tier", "Kuudra T$tier", AreaGroup.KUUDRA))
        for (island in Island.entries) {
            if (island == Island.Unknown || island == Island.Dungeon || island == Island.Kuudra) continue
            add(WaypointArea("ISLAND_${island.name}", island.displayName, AreaGroup.ISLANDS))
        }
    }

    val byKey: Map<String, WaypointArea> = all.associateBy { it.key }

    private val islandAreas = EnumMap<Island, WaypointArea>(Island::class.java).apply {
        for (island in Island.entries) byKey["ISLAND_${island.name}"]?.let { put(island, it) }
    }
    private val kuudraAreas = (1..5).map { byKey.getValue("KUUDRA_T$it") }
    private val floorAreas = (1..5).map { byKey.getValue("FLOOR_$it") }
    private val bossAreas = bossPhases.mapValues { (floor, phases) -> phases.associateWith { byKey.getValue("F${floor}_${it.uppercase()}") } }

    private var lastSplit: String? = null
    private var lastSplitClean: String = ""

    fun current(): WaypointArea? = when (val island = LocationUtils.currentArea) {
        Island.Unknown -> null
        Island.Dungeon -> dungeonArea()
        Island.Kuudra -> kuudraAreas.getOrNull(KuudraUtils.kuudraTier - 1)
        else -> islandAreas[island]
    }

    private fun dungeonArea(): WaypointArea? {
        if (!DungeonListener.inBoss) return null
        val floor = DungeonListener.floor?.floorNumber ?: return null
        if (floor in 1..5) return floorAreas[floor - 1]

        val areas = bossAreas[floor] ?: return null
        val phase = SplitsManager.currentSplitName()?.let(::stripped) ?: if (floor == 7) f7PhaseFromPosition() else null
        return areas[phase] ?: areas.values.first()
    }

    private fun stripped(split: String): String {
        if (split != lastSplit) {
            lastSplit = split
            lastSplitClean = split.noControlCodes
        }
        return lastSplitClean
    }

    private fun f7PhaseFromPosition(): String = when (DungeonUtils.getF7Phase()) {
        M7Phases.P2 -> "Storm"
        M7Phases.P3 -> "Terminals"
        M7Phases.P4 -> "Necron"
        M7Phases.P5 -> "Cleared"
        else -> "Maxor"
    }

    fun label(key: String): String = byKey[key]?.label ?: "§cUnknown ($key)"
}