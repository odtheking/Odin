package me.odinmain.features.impl.dungeon.puzzlesolvers

import me.odinmain.OdinMain.mc
import me.odinmain.events.impl.RenderEntityModelEvent
import me.odinmain.utils.addVec
import me.odinmain.utils.noControlCodes
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.RenderUtils.renderBoundingBox
import me.odinmain.utils.render.Renderer
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.monster.EntityBlaze
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object BlazeSolver {

    private var gotblazes = false
    private val hpMap = mutableMapOf<EntityArmorStand, Int>()
    val blazes = mutableListOf<EntityArmorStand>()
    private val blazeRegex = Regex("^\\[Lv15] Blaze [\\d,]+/([\\d,]+)❤\$")

    fun getRoomType() {
        getBlazes()
    }

    private fun getBlazes() {
        mc.theWorld?.loadedEntityList?.filterIsInstance<EntityArmorStand>()?.forEach { entity ->
            val health = blazeRegex.matchEntire(entity.name.noControlCodes)?.groups?.get(1)?.value ?: return@forEach
            val hp = health.replace(",", "").toIntOrNull() ?: return@forEach
            hpMap[entity] = hp
            blazes.add(entity)
        }
        if (blazes.isEmpty()) return

        blazes.sortBy { hpMap[it] }
        if (DungeonUtils.currentRoomName == "Lower Blaze") blazes.reverse()
        gotblazes = true
    }

    fun resetBlazes() {
        blazes.clear()
        gotblazes = false
    }

    fun renderBlazes() {
        if (blazes.isEmpty() && !gotblazes && (DungeonUtils.currentRoomName == "Lower Blaze" || DungeonUtils.currentRoomName == "Higher Blaze")) return getBlazes()
        if (blazes.isEmpty()) return
        blazes.forEachIndexed { index, entity ->
            val color = when (index) {
                0 -> Color.GREEN
                1 -> Color.ORANGE
                else -> Color.WHITE
            }
            Renderer.drawBox(entity.renderBoundingBox.expand(0.3,1.0,0.3).offset(0.0,-1.0,0.0), color, fillAlpha = 0f)
            if (blazes.indexOf(entity) in 1..2)Renderer.draw3DLine(blazes[index - 1].positionVector.addVec(0.0,-1,0.0), entity.positionVector.addVec(0.0,-1,0.0), color, 1f, false)
        }
    }

    fun removeBlaze() {
        blazes.removeFirstOrNull()
    }

}
