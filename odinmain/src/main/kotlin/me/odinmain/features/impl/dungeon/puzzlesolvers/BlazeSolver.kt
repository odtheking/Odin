package me.odinmain.features.impl.dungeon.puzzlesolvers

import me.odinmain.OdinMain.mc
import me.odinmain.events.impl.EnteredDungeonRoomEvent
import me.odinmain.events.impl.PostEntityMetadata
import me.odinmain.utils.*
import me.odinmain.utils.render.RenderUtils.renderVec
import me.odinmain.utils.render.Renderer
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.util.AxisAlignedBB
import kotlin.collections.forEachIndexed
import kotlin.collections.mutableListOf
import kotlin.collections.mutableMapOf
import kotlin.collections.removeAll
import kotlin.collections.set
import kotlin.collections.sortBy
import kotlin.collections.sortByDescending

object BlazeSolver {
    private val hpMap = mutableMapOf<EntityArmorStand, Int>()
    private var blazes = mutableListOf<EntityArmorStand>()
    private var roomType = 0

    fun getRoomType(event: EnteredDungeonRoomEvent) {
        if (event.room?.room?.data?.name == "Lower Blaze") roomType = 1
    }

    fun getBlaze(event: PostEntityMetadata) {
        val entity = mc.theWorld.getEntityByID(event.packet.entityId) ?: return

        if (entity !is EntityArmorStand || entity in blazes || !DungeonUtils.inDungeons) return
        val matchResult = Regex("""^\[Lv15] Blaze [\d,]+/([\d,]+)❤$""").find(entity.name.noControlCodes) ?: return
        hpMap[entity] = matchResult.groups[1]?.value?.replace(",", "")?.toIntOrNull() ?: return
        blazes.add(entity)
        if (blazes.isEmpty()) return

        if (roomType == 0) blazes.sortBy { hpMap[it] }
        else blazes.sortByDescending { hpMap[it] }
    }

    fun renderBlazes() {
        if (blazes.isEmpty()) return
        blazes.removeAll {
            mc.theWorld.getEntityByID(it.entityId) == null
        }
        blazes.forEachIndexed { index, entity ->
            val color = when (index) {
                0 -> PuzzleSolvers.blazeFirstColor
                1 -> PuzzleSolvers.blazeSecondColor
                else -> PuzzleSolvers.blazeAllColor
            }
            val aabb = AxisAlignedBB(-0.5, -2.0, -0.5, 0.5, 0.0, 0.5).offset(entity.renderVec)
            Renderer.drawBox(aabb, color, outlineAlpha = color.alpha, fillAlpha = 0f)

            if (PuzzleSolvers.blazeLineNext && index > 0 && index <= PuzzleSolvers.blazeLineAmount)
                Renderer.draw3DLine(blazes[index - 1].renderVec, aabb.middle, color, 1f, false)
        }
    }

    fun reset() {
        blazes.clear()
        hpMap.clear()
        roomType = 0
    }
}