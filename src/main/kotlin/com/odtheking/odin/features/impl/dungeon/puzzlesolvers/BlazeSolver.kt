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
import java.util.*

object BlazeSolver {
    private val blazes = TreeMap<Int, ArmorStand>()
    private var descending = false
    private val blazeHealthRegex = Regex("^\\[Lv+\\d+]  Blaze [\\d,]+/([\\d,]+)❤$")

    inline val inBlazeRoom get() = DungeonUtils.currentRoomName.equalsOneOf("Lower Blaze", "Higher Blaze")

    fun isBlazeTag(entity: Entity): Boolean =
        entity is ArmorStand && inBlazeRoom && entity in blazes.values

    fun onEntityData(event: EntityEvent.SetData) {
        if (!DungeonUtils.inClear) return
        val entity = event.entity as? ArmorStand ?: return
        val name = entity.name.string.takeIf { it.endsWith('❤') } ?: return
        val maxHealth = blazeHealthRegex.find(name)?.groupValues?.get(1)?.replace(",", "")?.toIntOrNull() ?: return
        if (blazes[maxHealth] == entity) return
        blazes.values.remove(entity)
        blazes[maxHealth] = entity
    }

    fun onRoomEnter(event: RoomEnterEvent) {
        descending = event.room?.data?.name == "Lower Blaze"
    }

    fun onEntityRemove(event: EntityEvent.Remove) {
        blazes.values.remove(event.entity)
    }

    fun onEntityDeath(event: EntityEvent.Event, sendComplete: Boolean) {
        if (event.id != 3.toByte() || event.entity !is Blaze) return
        blazes.values.removeIf { it.id == event.entity.id + 1 }
        if (blazes.isNotEmpty() || !inBlazeRoom) return

        DungeonListener.puzzles.find { it == Puzzle.BLAZE }?.status = PuzzleStatus.Completed
        onPuzzleComplete(DungeonUtils.currentRoomName)
        if (sendComplete) sendCommand("pc Blaze puzzle solved!")
    }

    fun onRenderWorld(event: RenderExtractEvent, blazeLineNext: Boolean, blazeLineAmount: Int, blazeStyle: BoxStyle, blazeFirstColor: Color, blazeSecondColor: Color, blazeThirdColor: Color, blazeAllColor: Color, blazeLineWidth: Float) {
        if (blazes.isEmpty() || !inBlazeRoom) return

        var previous: ArmorStand? = null
        (if (descending) blazes.descendingMap().values else blazes.values).forEachIndexed { index, stand ->
            val color = when (index) {
                0 -> blazeFirstColor
                1 -> blazeSecondColor
                2 -> blazeThirdColor
                else -> blazeAllColor
            }
            val aabb = stand.boundingBox.inflate(0.5, 1.0, 0.5).move(0.0, -1.0, 0.0)

            event.drawStyledBox(aabb, color, blazeStyle, depth = true)

            if (blazeLineNext && index <= blazeLineAmount) previous?.let {
                event.drawLine(listOf(it.boundingBox.center, aabb.center), color = color, thickness = blazeLineWidth, depth = true)
            }
            previous = stand
        }
    }

    fun reset() {
        blazes.clear()
    }
}
