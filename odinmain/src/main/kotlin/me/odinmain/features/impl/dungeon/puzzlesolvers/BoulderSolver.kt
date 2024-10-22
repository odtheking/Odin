package me.odinmain.features.impl.dungeon.puzzlesolvers

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import me.odinmain.events.impl.DungeonEvents
import me.odinmain.utils.*
import me.odinmain.utils.render.Renderer
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.skyblock.getBlockIdAt
import net.minecraft.util.BlockPos
import net.minecraftforge.event.entity.player.PlayerInteractEvent
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

object BoulderSolver {
    private data class BoxPosition(val render: BlockPos, val click: BlockPos)
    private var currentPositions = mutableListOf<BoxPosition>()
    private var solutions: Map<String, List<List<Int>>>
    private val gson = GsonBuilder().setPrettyPrinting().create()
    private val isr = this::class.java.getResourceAsStream("/boulderSolutions.json")?.let { InputStreamReader(it, StandardCharsets.UTF_8) }

    init {
        try {
            val text = isr?.readText()
            solutions = gson.fromJson(text, object : TypeToken<Map<String, List<List<Int>>>>() {}.type)
            isr?.close()
        } catch (e: Exception) {
            e.printStackTrace()
            solutions = emptyMap()
        }
    }

    fun onRoomEnter(event: DungeonEvents.RoomEnterEvent) {
        val room = event.fullRoom?.room ?: return reset()
        if (room.data.name != "Boulder") return reset()
        var str = ""
        for (z in -3..2) {
            for (x in -3..3) {
                room.vec2.addRotationCoords(room.rotation, x * 3, z * 3).let { str += if (getBlockIdAt(BlockPos(it.x, 66, it.z)) == 0) "0" else "1" }
            }
        }
        currentPositions = solutions[str]?.map { sol ->
            val render = room.vec2.addRotationCoords(room.rotation, sol[0], sol[1]).let { BlockPos(it.x, 65, it.z) }
            val click = room.vec2.addRotationCoords(room.rotation, sol[2], sol[3]).let { BlockPos(it.x, 65, it.z) }
            BoxPosition(render, click)
        }?.toMutableList() ?: return
    }

    fun onRenderWorld() {
        if (DungeonUtils.currentRoomName != "Boulder" || currentPositions.isEmpty()) return
        if (PuzzleSolvers.showAllBoulderClicks) currentPositions.forEach {
            Renderer.drawStyledBlock(it.render, PuzzleSolvers.boulderColor, PuzzleSolvers.boulderStyle, PuzzleSolvers.boulderLineWidth)
        }
        else currentPositions.firstOrNull()?.let {
            Renderer.drawStyledBlock(it.render, PuzzleSolvers.boulderColor, PuzzleSolvers.boulderStyle, PuzzleSolvers.boulderLineWidth)
        }
    }

    fun playerInteract(event: PlayerInteractEvent) {
        if (event.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK && getBlockIdAt(event.pos).equalsOneOf(77, 323))
            currentPositions.removeFirstOrNull { it.click == event.pos }
    }

    fun reset() {
        currentPositions = mutableListOf()
    }
}