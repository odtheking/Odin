package com.odtheking.odin.features.impl.dungeon.puzzlesolvers

import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.events.BlockUpdateEvent
import com.odtheking.odin.events.RenderEvent
import com.odtheking.odin.events.RoomEnterEvent
import com.odtheking.odin.features.impl.dungeon.puzzlesolvers.PuzzleSolvers.onPuzzleComplete
import com.odtheking.odin.utils.Color
import com.odtheking.odin.utils.Color.Companion.withAlpha
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.JsonResourceLoader
import com.odtheking.odin.utils.equalsOneOf
import com.odtheking.odin.utils.render.drawLine
import com.odtheking.odin.utils.render.drawStyledBox
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import com.odtheking.odin.utils.skyblock.dungeon.map.tile.DungeonRoom
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.phys.AABB
import java.util.concurrent.ConcurrentHashMap

object BeamsSolver {
    private var scanned = false
    private var lanternPairs: List<List<Int>> = JsonResourceLoader.loadJson(
        "/assets/odin/puzzles/creeperBeamsSolutions.json", emptyList()
    )

    private var currentLanternPairs = ConcurrentHashMap<BlockPos, Pair<BlockPos, Color>>()

    fun onRoomEnter(event: RoomEnterEvent) = with(event.room) {
        if (this?.data?.name != "Creeper Beams") return@with reset()
        recalculateLanternPairs(this)
    }

    private fun recalculateLanternPairs(room: DungeonRoom) {
        currentLanternPairs.clear()
        lanternPairs.forEachIndexed { index, list ->
            val pos = room.getRealCoords(BlockPos(list[0], list[1], list[2])).takeIf { mc.level?.getBlockState(it)?.block == Blocks.SEA_LANTERN } ?: return@forEachIndexed
            val pos2 = room.getRealCoords(BlockPos(list[3], list[4], list[5])).takeIf { mc.level?.getBlockState(it)?.block == Blocks.SEA_LANTERN } ?: return@forEachIndexed

            currentLanternPairs[pos] = pos2 to colors[index % colors.size]
        }
    }

    fun onRenderWorld(event: RenderEvent.Extract, beamStyle: Int, beamsTracer: Boolean, beamsAlpha: Float) {
        if (DungeonUtils.currentRoomName != "Creeper Beams" || currentLanternPairs.isEmpty()) return

        currentLanternPairs.entries.forEach { positions ->
            val color = positions.value.second.withAlpha(beamsAlpha)

            event.drawStyledBox(AABB(positions.key), color, depth = true, style = beamStyle)
            event.drawStyledBox(AABB(positions.value.first), color, depth = true, style = beamStyle)

            if (beamsTracer)
                event.drawLine(listOf(positions.key.center, positions.value.first.center), color = color, depth = false)
        }
    }

    fun onBlockChange(event: BlockUpdateEvent) {
        if (DungeonUtils.currentRoomName != "Creeper Beams") return
        if (event.pos == DungeonUtils.currentRoom?.getRealCoords(BlockPos(15, 69, 15)) && event.old.isAir && event.updated.block == Blocks.CHEST) onPuzzleComplete("Creeper Beams")
        if (
            event.old.block.equalsOneOf(Blocks.PRISMARINE, Blocks.SEA_LANTERN) &&
            event.updated.block.equalsOneOf(Blocks.PRISMARINE, Blocks.SEA_LANTERN)
        ) {
            mc.execute {
                recalculateLanternPairs(DungeonUtils.currentRoom ?: return@execute)
            }
            return
        }
    }

    fun reset() {
        scanned = false
        currentLanternPairs.clear()
    }

    private val colors = listOf(Colors.MINECRAFT_GOLD, Colors.MINECRAFT_GREEN, Colors.MINECRAFT_LIGHT_PURPLE, Colors.MINECRAFT_DARK_AQUA, Colors.MINECRAFT_YELLOW, Colors.MINECRAFT_DARK_RED, Colors.WHITE, Colors.MINECRAFT_DARK_PURPLE)
}

