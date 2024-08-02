package me.odinmain.features.impl.dungeon.puzzlesolvers

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import me.odinmain.OdinMain.logger
import me.odinmain.events.impl.BlockChangeEvent
import me.odinmain.events.impl.DungeonEvents.RoomEnterEvent
import me.odinmain.features.impl.dungeon.puzzlesolvers.PuzzleSolvers.beamsAlpha
import me.odinmain.ui.clickgui.util.ColorUtil.withAlpha
import me.odinmain.utils.*
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.Renderer
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.skyblock.getBlockIdAt
import net.minecraft.init.Blocks
import net.minecraft.util.BlockPos
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

object BeamsSolver {
    private var scanned = false
    private var lanternPairs: List<List<Int>>
    private val gson = GsonBuilder().setPrettyPrinting().create()
    private val isr = this::class.java.getResourceAsStream("/creeperBeamsSolutions.json")
        ?.let { InputStreamReader(it, StandardCharsets.UTF_8) }

    init {
        try {
            val text = isr?.readText()
            lanternPairs = gson.fromJson(text, object : TypeToken<List<List<Int>>>() {}.type)
            isr?.close()
        } catch (e: Exception) {
            logger.error("Error loading creeper beams solutions", e)
            lanternPairs = emptyList()
        }
    }

    private var currentLanternPairs = mutableMapOf<BlockPos, Pair<BlockPos, Color>>()

    fun enterDungeonRoom(event: RoomEnterEvent) {
        val room = event.fullRoom?.room ?: return // <-- orb = orb.orb
        if (room.data.name != "Creeper Beams") return reset()

        currentLanternPairs.clear()
        lanternPairs.forEach {
            val pos = room.vec2.addRotationCoords(room.rotation, x = it[0], z = it[2]).let { vec -> BlockPos(vec.x, it[1], vec.z) }

            val pos2 = room.vec2.addRotationCoords(room.rotation, x = it[3], z = it[5]).let { vec -> BlockPos(vec.x, it[4], vec.z) }

            if (getBlockIdAt(pos) == 169 && getBlockIdAt(pos2) == 169)
                currentLanternPairs[pos] = pos2 to colors[currentLanternPairs.size]
        }
    }

    fun onRenderWorld() {
        if (DungeonUtils.currentRoomName != "Creeper Beams" || currentLanternPairs.isEmpty()) return
        val finalLanternPairs = currentLanternPairs.toMap()
        finalLanternPairs.entries.forEach { positions ->
            val color = positions.value.second

            Renderer.drawBox(positions.key.toAABB(), color, depth = PuzzleSolvers.beamsDepth, outlineAlpha = if (PuzzleSolvers.beamStyle == 0) 0 else color.alpha, fillAlpha = if (PuzzleSolvers.beamStyle == 1) 0 else beamsAlpha)
            Renderer.drawBox(positions.value.first.toAABB(), color, depth = PuzzleSolvers.beamsDepth, outlineAlpha = if (PuzzleSolvers.beamStyle == 0) 0 else color.alpha, fillAlpha = if (PuzzleSolvers.beamStyle == 1) 0 else beamsAlpha)

            if (PuzzleSolvers.beamsTracer)
                Renderer.draw3DLine(positions.key.toVec3().addVec(0.5, 0.5, 0.5), positions.value.first.toVec3().addVec(0.5, 0.5, 0.5), color = color.withAlpha(beamsAlpha), depth = PuzzleSolvers.beamsDepth, lineWidth = 1f)
        }
    }

    fun onBlockChange(event: BlockChangeEvent) {
        currentLanternPairs.entries.filter {
            event.pos.equalsOneOf(it.key, it.value.first) &&
            event.update.block != Blocks.sea_lantern &&
            event.old.block == Blocks.sea_lantern
        }.forEach { currentLanternPairs.remove(it.key) }
    }

    fun reset() {
        scanned = false
        currentLanternPairs.clear()
    }

    private val colors = listOf(
        Color.ORANGE, Color.GREEN, Color.PINK, Color.CYAN, Color.YELLOW, Color.DARK_RED, Color.WHITE, Color.PURPLE
    )
}

