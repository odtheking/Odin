package com.odtheking.odin.features.impl.dungeon.puzzlesolvers

import com.odtheking.odin.events.EntityEvent
import com.odtheking.odin.events.RenderExtractEvent
import com.odtheking.odin.events.RoomEnterEvent
import com.odtheking.odin.features.impl.dungeon.puzzlesolvers.PuzzleSolvers.onPuzzleComplete
import com.odtheking.odin.utils.Color
import com.odtheking.odin.utils.equalsOneOf
import com.odtheking.odin.utils.render.BoxStyle
import com.odtheking.odin.utils.render.drawLine
import com.odtheking.odin.utils.render.drawStyledBox
import com.odtheking.odin.utils.sendCommand
import com.odtheking.odin.utils.skyblock.dungeon.DungeonListener
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import com.odtheking.odin.utils.skyblock.dungeon.Puzzle
import com.odtheking.odin.utils.skyblock.dungeon.PuzzleStatus
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.decoration.ArmorStand
import net.minecraft.world.entity.monster.Blaze

object BlazeSolver {
    private class BlazeTag(val entity: ArmorStand, val maxHealth: Int)

    private val blazes = ArrayList<BlazeTag>(10)
    private val blazeHealthRegex = Regex("^\\[Lv+\\d+]  Blaze [\\d,]+/([\\d,]+)❤$")

    inline val inBlazeRoom get() = DungeonUtils.currentRoomName.equalsOneOf("Lower Blaze", "Higher Blaze")

    fun isBlazeTag(entity: Entity): Boolean =
        entity is ArmorStand && inBlazeRoom && blazes.any { it.entity.id == entity.id }

    fun onEntityData(event: EntityEvent.SetData) {
        if (!DungeonUtils.inClear) return
        val entity = (event.entity as? ArmorStand)?.takeIf { !it.isDeadOrDying } ?: return
        if (blazes.any { it.entity.uuid == entity.uuid }) return
        val name = entity.name.string.takeIf { it.endsWith('❤') } ?: return
        val maxHealth = blazeHealthRegex.find(name)?.groupValues?.get(1)?.replace(",", "")?.toIntOrNull() ?: return
        blazes.add(BlazeTag(entity, maxHealth))
        sort(DungeonUtils.currentRoomName)
    }

    private fun sort(roomName: String?) {
        if (roomName == "Lower Blaze") blazes.sortByDescending { it.maxHealth }
        else blazes.sortBy { it.maxHealth }
    }

    fun onRoomEnter(event: RoomEnterEvent) {
        sort(event.room?.data?.name)
    }

    fun onEntityRemove(event: EntityEvent.Remove) {
        blazes.removeAll { it.entity.id == event.entity.id }
    }

    fun onEntityDeath(event: EntityEvent.Event, sendComplete: Boolean) {
        if (event.id != 3.toByte() || event.entity !is Blaze) return
        if (!blazes.removeAll { it.entity.id == event.entity.id + 1 } || blazes.isNotEmpty() || !inBlazeRoom) return

        DungeonListener.puzzles.find { it == Puzzle.BLAZE }?.status = PuzzleStatus.Completed
        onPuzzleComplete(DungeonUtils.currentRoomName)
        if (sendComplete) sendCommand("pc Blaze puzzle solved!")
    }

    fun onRenderWorld(event: RenderExtractEvent, blazeLineNext: Boolean, blazeLineAmount: Int, blazeStyle: BoxStyle, blazeFirstColor: Color, blazeSecondColor: Color, blazeThirdColor: Color, blazeAllColor: Color, blazeLineWidth: Float) {
        if (blazes.isEmpty() || !inBlazeRoom) return

        blazes.forEachIndexed { index, tag ->
            val color = when (index) {
                0 -> blazeFirstColor
                1 -> blazeSecondColor
                2 -> blazeThirdColor
                else -> blazeAllColor
            }
            val aabb = tag.entity.boundingBox.inflate(0.5, 1.0, 0.5).move(0.0, -1.0, 0.0)

            event.drawStyledBox(aabb, color, blazeStyle, depth = true)

            if (blazeLineNext && index > 0 && index <= blazeLineAmount)
                event.drawLine(listOf(blazes[index - 1].entity.boundingBox.center, aabb.center), color = color, thickness = blazeLineWidth, depth = true)
        }
    }

    fun reset() {
        blazes.clear()
    }
}
