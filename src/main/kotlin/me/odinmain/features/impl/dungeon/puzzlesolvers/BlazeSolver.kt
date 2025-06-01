package me.odinmain.features.impl.dungeon.puzzlesolvers

import me.odinmain.OdinMain.mc
import me.odinmain.features.impl.dungeon.puzzlesolvers.PuzzleSolvers.onPuzzleComplete
import me.odinmain.utils.equalsOneOf
import me.odinmain.utils.middle
import me.odinmain.utils.noControlCodes
import me.odinmain.utils.offset
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.HighlightRenderer.HighlightEntity
import me.odinmain.utils.render.RenderUtils.renderBoundingBox
import me.odinmain.utils.render.RenderUtils.renderVec
import me.odinmain.utils.render.Renderer
import me.odinmain.utils.skyblock.LocationUtils
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.skyblock.dungeon.Puzzle
import me.odinmain.utils.skyblock.dungeon.PuzzleStatus
import me.odinmain.utils.skyblock.partyMessage
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.monster.EntityBlaze
import net.minecraft.util.AxisAlignedBB
import kotlin.collections.set

object BlazeSolver {
    private var blazes = mutableListOf<EntityBlaze>()
    private var roomType = 0
    private var lastBlazeCount = 10
    private val blazeHealthRegex = Regex("^\\[Lv15] Blaze [\\d,]+/([\\d,]+)❤$")

    fun getBlaze() {
        val room = DungeonUtils.currentRoom ?: return
        if (!DungeonUtils.inDungeons || !room.data.name.equalsOneOf("Lower Blaze", "Higher Blaze")) return
        val hpMap = mutableMapOf<EntityBlaze, Int>()
        blazes.clear()
        mc.theWorld?.loadedEntityList?.forEach { entity ->
            if (entity !is EntityArmorStand) return@forEach
            val blaze = mc.theWorld.getEntitiesWithinAABBExcludingEntity(entity, entity.entityBoundingBox.offset(0.0, -1.0, 0.0))
                .filterIsInstance<EntityBlaze>().firstOrNull() ?: return@forEach
            hpMap[blaze] = blazeHealthRegex.find(entity.name.noControlCodes)?.groups?.get(1)?.value?.replace(",", "")?.toIntOrNull() ?: return@forEach
            if (blaze !in blazes) blazes.add(blaze)
        }
        if (room.data.name == "Lower Blaze") blazes.sortByDescending { hpMap[it] }
        else blazes.sortBy { hpMap[it] }
    }

    fun getHighlightedBlazes(blazeFirstColor: Color, blazeSecondColor: Color, blazeAllColor: Color, thickness: Float, depth: Boolean, boxStyle: Int): Collection<HighlightEntity> {
        return blazes.mapIndexed { index, blaze ->
            val color = when (index) {
                0 -> blazeFirstColor
                1 -> blazeSecondColor
                else -> blazeAllColor
            }
            HighlightEntity(blaze, color, thickness, depth, boxStyle)
        }
    }

    fun onRenderWorld(blazeLineNext: Boolean, blazeLineAmount: Int, blazeStyle: Int, blazeFirstColor: Color, blazeSecondColor: Color, blazeAllColor: Color, blazeWidth: Float, blazeHeight: Float, blazeSendComplete: Boolean, blazeLineWidth: Float) {
        if (!DungeonUtils.currentRoomName.equalsOneOf("Lower Blaze", "Higher Blaze")) return
        if (blazes.isEmpty()) return
        blazes.removeAll { mc.theWorld?.getEntityByID(it.entityId) == null }
        if (blazes.isEmpty() && lastBlazeCount == 1) {
            LocationUtils.currentDungeon?.puzzles?.find { it == Puzzle.BLAZE }?.status = PuzzleStatus.Completed
            onPuzzleComplete(if (DungeonUtils.currentRoomName == "Higher Blaze") "Higher Blaze" else "Lower Blaze")
            if (blazeSendComplete) partyMessage("Blaze puzzle solved!")
            lastBlazeCount = 0
            return
        }
        lastBlazeCount = blazes.size
        blazes.forEachIndexed { index, entity ->
            val color = when (index) {
                0 -> blazeFirstColor
                1 -> blazeSecondColor
                else -> blazeAllColor
            }
            //val aabb = AxisAlignedBB(-blazeWidth / 2.0, -1 - (blazeHeight / 2.0), -blazeWidth / 2.0, blazeWidth / 2.0, (blazeHeight / 2.0) - 1, blazeWidth / 2.0).offset(entity.positionVector)

            //Renderer.drawStyledBox(aabb, color, blazeStyle, depth = true)

            if (blazeLineNext && index > 0 && index <= blazeLineAmount)
                Renderer.draw3DLine(listOf(blazes[index - 1].renderVec, entity.renderBoundingBox.middle), color = color, lineWidth = blazeLineWidth, depth = true)
        }
    }

    fun reset() {
        blazes.clear()
        roomType = 0
        lastBlazeCount = 10
    }
}
