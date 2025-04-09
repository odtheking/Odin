package me.odinmain.features.impl.dungeon.puzzlesolvers

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import me.odinmain.OdinMain.logger
import me.odinmain.events.impl.BlockChangeEvent
import me.odinmain.events.impl.RoomEnterEvent
import me.odinmain.features.impl.dungeon.puzzlesolvers.PuzzleSolvers.onPuzzleComplete
import me.odinmain.utils.addVec
import me.odinmain.utils.equalsOneOf
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.Renderer
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.skyblock.dungeon.DungeonUtils.getRealCoords
import me.odinmain.utils.skyblock.getBlockIdAt
import me.odinmain.utils.toVec3
import me.odinmain.utils.ui.Colors
import me.odinmain.utils.ui.clickgui.util.ColorUtil.withAlpha
import net.minecraft.init.Blocks
import net.minecraft.util.BlockPos
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.util.concurrent.ConcurrentHashMap

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

    private var currentLanternPairs = ConcurrentHashMap<BlockPos, Pair<BlockPos, Color>>()

    fun onRoomEnter(event: RoomEnterEvent) = with(event.room) {
        if (this?.data?.name != "Creeper Beams") return reset()

        currentLanternPairs.clear()
        lanternPairs.forEach { list ->
            val pos = getRealCoords(list[0], list[1], list[2]).takeIf { getBlockIdAt(it) == 169 } ?: return@forEach
            val pos2 = getRealCoords(list[3], list[4], list[5]).takeIf { getBlockIdAt(it) == 169 } ?: return@forEach

            currentLanternPairs[pos] = pos2 to colors[currentLanternPairs.size]
        }
    }

    fun onRenderWorld(beamStyle: Int, beamsTracer: Boolean, beamsAlpha: Float) {
        if (DungeonUtils.currentRoomName != "Creeper Beams" || currentLanternPairs.isEmpty()) return

        currentLanternPairs.entries.forEach { positions ->
            val color = positions.value.second

            Renderer.drawStyledBlock(positions.key, color, depth = true, style = beamStyle)
            Renderer.drawStyledBlock(positions.value.first, color, depth = true, style = beamStyle)

            if (beamsTracer)
                Renderer.draw3DLine(listOf(positions.key.toVec3().addVec(0.5, 0.5, 0.5), positions.value.first.toVec3().addVec(0.5, 0.5, 0.5)), color = color.withAlpha(beamsAlpha), depth = false, lineWidth = 2f)
        }
    }

    fun onBlockChange(event: BlockChangeEvent) {
        if (DungeonUtils.currentRoomName != "Creeper Beams") return
        if (event.pos == DungeonUtils.currentRoom?.getRealCoords(15, 69, 15) && event.old.block == Blocks.air && event.updated.block == Blocks.chest) onPuzzleComplete("Creeper Beams")
        currentLanternPairs.forEach { (key, value) ->
            if (event.pos.equalsOneOf(key, value.first) &&
                event.updated.block != Blocks.sea_lantern &&
                event.old.block == Blocks.sea_lantern) currentLanternPairs.remove(key)
        }
    }

    fun reset() {
        scanned = false
        currentLanternPairs.clear()
    }

    private val colors = listOf(Colors.MINECRAFT_GOLD, Colors.MINECRAFT_GREEN, Colors.MINECRAFT_LIGHT_PURPLE, Colors.MINECRAFT_DARK_AQUA, Colors.MINECRAFT_YELLOW, Colors.MINECRAFT_DARK_RED, Colors.WHITE, Colors.MINECRAFT_DARK_PURPLE)
}

