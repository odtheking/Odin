package com.odtheking.odin.features.impl.dungeon.puzzlesolvers

import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.events.RenderEvent
import com.odtheking.odin.features.impl.dungeon.puzzlesolvers.PuzzleSolvers.onPuzzleComplete
import com.odtheking.odin.utils.Color
import com.odtheking.odin.utils.equalsOneOf
import com.odtheking.odin.utils.render.drawLine
import com.odtheking.odin.utils.render.drawStyledBox
import com.odtheking.odin.utils.renderPos
import com.odtheking.odin.utils.sendCommand
import com.odtheking.odin.utils.skyblock.dungeon.DungeonListener
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import com.odtheking.odin.utils.skyblock.dungeon.Puzzle
import com.odtheking.odin.utils.skyblock.dungeon.PuzzleStatus
import net.minecraft.world.entity.decoration.ArmorStand

object BlazeSolver {
    private var blazes = mutableListOf<ArmorStand>()
    private var roomType = 0
    private var lastBlazeCount = 10
    private val blazeHealthRegex = Regex("^\\[Lv15] ♨ Blaze [\\d,]+/([\\d,]+)❤$")

    fun getBlaze() {
        if (!DungeonUtils.inDungeons || DungeonUtils.currentRoom?.data?.name?.equalsOneOf("Lower Blaze", "Higher Blaze") == false) return
        val hpMap = mutableMapOf<ArmorStand, Int>()
        blazes.clear()
        mc.level?.entitiesForRendering()?.forEach { entity ->
            if (entity !is ArmorStand || entity in blazes) return@forEach
            val hp = blazeHealthRegex.find(entity.name.string)?.groups?.get(1)?.value?.replace(",", "")?.toIntOrNull() ?: return@forEach
            hpMap[entity] = hp
            blazes.add(entity)
        }
        if (DungeonUtils.currentRoom?.data?.name == "Lower Blaze") blazes.sortByDescending { hpMap[it] }
        else blazes.sortBy { hpMap[it] }
    }

    fun onRenderWorld(event: RenderEvent.Extract, blazeLineNext: Boolean, blazeLineAmount: Int, blazeStyle: Int, blazeFirstColor: Color, blazeSecondColor: Color, blazeThirdColor: Color, blazeAllColor: Color, blazeSendComplete: Boolean, blazeLineWidth: Float) {
        if (!DungeonUtils.currentRoomName.equalsOneOf("Lower Blaze", "Higher Blaze")) return
        if (blazes.isEmpty()) return
        blazes.removeAll { mc.level?.getEntity(it.id) == null }
        if (blazes.isEmpty() && lastBlazeCount == 1) {
            DungeonListener.puzzles.find { it == Puzzle.BLAZE }?.status = PuzzleStatus.Completed
            onPuzzleComplete(DungeonUtils.currentRoomName)
            if (blazeSendComplete) sendCommand("pc Blaze puzzle solved!")
            lastBlazeCount = 0
            return
        }
        lastBlazeCount = blazes.size
        blazes.forEachIndexed { index, entity ->
            val color = when (index) {
                0 -> blazeFirstColor
                1 -> blazeSecondColor
                2 -> blazeThirdColor
                else -> blazeAllColor
            }
            val aabb = entity.boundingBox.inflate(0.5, 1.0, 0.5).move(0.0, -1.0, 0.0)

            event.drawStyledBox(aabb, color, blazeStyle, depth = true)

            if (blazeLineNext && index > 0 && index <= blazeLineAmount)
                event.drawLine(listOf(blazes[index - 1].renderPos, aabb.center), color = color, thickness = blazeLineWidth, depth = true)
        }
    }

    fun reset() {
        lastBlazeCount = 10
        blazes.clear()
        roomType = 0
    }
}
