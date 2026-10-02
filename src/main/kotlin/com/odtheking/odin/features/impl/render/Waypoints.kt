package com.odtheking.odin.features.impl.render

import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.events.LevelEvent
import com.odtheking.odin.events.MessageEvent
import com.odtheking.odin.events.RenderExtractEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.Color
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.modMessage
import com.odtheking.odin.utils.render.drawCustomBeacon
import net.minecraft.core.BlockPos
import kotlin.math.abs

object Waypoints : Module(
    name = "Waypoints",
    description = "Allows to render waypoints based on coordinates in chat."
) {
    private val fromParty by BooleanSetting("From Party Chat", true, desc = "Adds waypoints from party chat.")
    private val fromAll by BooleanSetting("From All Chat", false, desc = "Adds waypoints from all chat.")
    private val personalWaypoint by BooleanSetting("Personal Waypoint", false, desc = "Makes waypoints you send also create for you.")

    private val partyRegex =
        Regex("^Party > (?:\\[[^]]*?])? ?(\\w{1,16})(?: [ቾ⚒])?: x: (-?\\d+), y: (-?\\d+), z: (-?\\d+).*") // https://regex101.com/r/8K26A1/1
    private val allRegex =
        Regex("^(?!Party >).*\\s(?:\\[[^]]*?])? ?(\\w{1,16})(?: [ቾ⚒])?: x: (-?\\d+),? y: (-?\\d+),? z: (-?\\d+).*") // https://regex101.com/r/A3aoyL/1

    private val temporaryWaypoints = mutableListOf<Waypoint>()

    init {
        on<MessageEvent.Chat> {
            val (name, x, y, z) = when {
                fromParty && partyRegex.matches(message) -> partyRegex.find(message)?.destructured
                fromAll && allRegex.matches(message) -> allRegex.find(message)?.destructured
                else -> null
            } ?: return@on

            if (name == mc.player?.name?.string && !personalWaypoint) return@on

            addTempWaypoint("§6$name", x.toIntOrNull() ?: return@on, y.toIntOrNull() ?: return@on, z.toIntOrNull() ?: return@on)
        }

        on<RenderExtractEvent> {
            temporaryWaypoints.removeAll {
                drawCustomBeacon(it.name, it.blockPos, it.color)
                System.currentTimeMillis() > it.timeAdded + it.duration
            }
        }

        on<LevelEvent.Load> {
            temporaryWaypoints.clear()
        }
    }

    fun addTempWaypoint(name: String = "Waypoint", x: Int, y: Int, z: Int, duration: Long = 60_000) {
        if (listOf(x, y, z).any { abs(it) > 5000 }) return modMessage("§cWaypoint out of bounds.")
        if (temporaryWaypoints.any { it.blockPos.x == x && it.blockPos.y == y && it.blockPos.z == z }) return modMessage("§cWaypoint already exists at $x, $y, $z.")
        modMessage("§aAdded temporary waypoint at §6$x§r, §3$y§r, §d$z§r.")
        temporaryWaypoints.add(Waypoint(name, BlockPos(x, y, z), colors.random(), duration))
    }

    private val colors = listOf(
        Colors.MINECRAFT_GOLD, Colors.MINECRAFT_GREEN,
        Colors.MINECRAFT_LIGHT_PURPLE, Colors.MINECRAFT_DARK_AQUA,
        Colors.MINECRAFT_YELLOW, Colors.MINECRAFT_DARK_RED,
        Colors.WHITE, Colors.MINECRAFT_DARK_PURPLE,  Colors.MINECRAFT_BLUE,
        Colors.MINECRAFT_YELLOW, Colors.MINECRAFT_RED,
        Colors.MINECRAFT_LIGHT_PURPLE, Colors.MINECRAFT_DARK_GREEN,
    )

    data class Waypoint(
        val name: String,
        val blockPos: BlockPos,
        val color: Color,
        val duration: Long,
        val timeAdded: Long = System.currentTimeMillis()
    )
}