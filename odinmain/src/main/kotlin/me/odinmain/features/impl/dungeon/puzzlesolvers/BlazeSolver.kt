package me.odinmain.features.impl.dungeon.puzzlesolvers

import me.odinmain.OdinMain.mc
import me.odinmain.utils.noControlCodes
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.RenderUtils.renderBoundingBox
import me.odinmain.utils.render.Renderer
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import net.minecraft.entity.item.EntityArmorStand

object BlazeSolver {

    private val hpMap = mutableMapOf<EntityArmorStand, Int>()
    private val blazes = mutableListOf<EntityArmorStand>()

    fun getRoomType() {
        getBlazes()
        if (DungeonUtils.currentRoomName == "Lower Blaze") {
            blazes.reverse()
        }
    }

    private fun getBlazes() {
        mc.theWorld?.loadedEntityList?.filterIsInstance<EntityArmorStand>()?.forEach { entity ->
            val matchResult = Regex("""^\[Lv15] Blaze [\d,]+/([\d,]+)❤$""").find(entity.name.noControlCodes) ?: return@forEach
            val (_, health) = matchResult.destructured
            val hp = health.replace(",", "").toInt()
            hpMap[entity] = hp
            blazes.add(entity)
        }
        if (blazes.isEmpty()) return

        blazes.sortBy { hpMap[it] }
    }

    fun resetBlazes() {
        blazes.clear()
    }

    fun renderBlazes() {
        blazes.forEachIndexed { index, entity ->
            val color = when (index) {
                0 -> Color.GREEN
                1 -> Color.ORANGE
                else -> Color.WHITE
            }
            Renderer.drawBox(entity.renderBoundingBox.addCoord(0.0, -2.0, 0.0), color, fillAlpha = 0f)
            // TODO: Make sure the index - 1 doesn't crash because of indexing to -1
            Renderer.draw3DLine(blazes[index - 1].positionVector, entity.positionVector, color, 1, false)
        }
    }

}
