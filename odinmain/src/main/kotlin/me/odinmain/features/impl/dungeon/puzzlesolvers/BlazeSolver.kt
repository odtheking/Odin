package me.odinmain.features.impl.dungeon.puzzlesolvers

import me.odinmain.OdinMain.mc
import me.odinmain.utils.*
import me.odinmain.utils.render.RenderUtils.renderVec
import me.odinmain.utils.render.Renderer
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.skyblock.getBlockIdAt
import me.odinmain.utils.skyblock.partyMessage
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.util.AxisAlignedBB
import kotlin.collections.set

object BlazeSolver {
    private var blazes = mutableListOf<EntityArmorStand>()
    private var roomType = 0

    fun getBlaze() {
        val room = DungeonUtils.currentRoom?.room ?: return
        if (!DungeonUtils.inDungeons || !room.data.name.equalsOneOf("Lower Blaze", "Higher Blaze")) return
        val hpMap = mutableMapOf<EntityArmorStand, Int>()
        blazes.clear()
        mc.theWorld.loadedEntityList.filterIsInstance<EntityArmorStand>().filter { it !in blazes }.forEach { entity ->
            val matchResult = Regex("""^\[Lv15] Blaze [\d,]+/([\d,]+)❤$""").find(entity.name.noControlCodes) ?: return@forEach
            val hp = matchResult.groups[1]?.value?.replace(",", "")?.toIntOrNull() ?: return@forEach
            hpMap[entity] = hp
            blazes.add(entity)
        }
        blazes.sortBy { hpMap[it] }
        if (getBlockIdAt(room.x + 1, 118, room.z) != 4) blazes.reverse()
    }

    fun renderBlazes() {
        if (blazes.isEmpty()) return
        blazes.removeAll {
            mc.theWorld.getEntityByID(it.entityId) == null
        }
        if (blazes.isEmpty() && PuzzleSolvers.blazeSendComplete) return partyMessage("Blaze puzzle solved!")
        blazes.forEachIndexed { index, entity ->
            val color = when (index) {
                0 -> PuzzleSolvers.blazeFirstColor
                1 -> PuzzleSolvers.blazeSecondColor
                else -> PuzzleSolvers.blazeAllColor
            }
            val aabb = AxisAlignedBB(-0.5, -2.0, -0.5, 0.5, 0.0, 0.5).offset(entity.positionVector)

            Renderer.drawBox(aabb, color,
                outlineAlpha = if (PuzzleSolvers.blazeStyle == 0) 0 else color.alpha, fillAlpha = if (PuzzleSolvers.blazeStyle == 1) 0 else color.alpha)

            if (PuzzleSolvers.blazeLineNext && index > 0 && index <= PuzzleSolvers.blazeLineAmount)
                Renderer.draw3DLine(blazes[index - 1].renderVec, entity.entityBoundingBox.middle, color, 1f, false)
        }
    }

    fun reset() {
        blazes.clear()
        roomType = 0
    }
}