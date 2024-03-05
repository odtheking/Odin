package me.odinmain.features.impl.dungeon

import me.odinmain.OdinMain.mc
import me.odinmain.utils.noControlCodes
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.RenderUtils
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
        val entities = mc.theWorld.loadedEntityList.filterIsInstance<EntityArmorStand>()
        entities.forEach { e ->
            val name = e.name.noControlCodes
            val regex = Regex("""^\[Lv15\] Blaze [\d,]+/([\d,]+)❤$""")
            val matchResult = regex.find(name)
            if (matchResult != null) {
                val (_, health) = matchResult.destructured
                val hp = health.replace(",", "").toInt()
                hpMap[e] = hp
                blazes.add(e)
            }
        }
        if (blazes.isEmpty()) return

        blazes.sortBy { hpMap[it] }
    }

    fun resetBlazes() {
        blazes.clear()
    }

    fun renderBlazes() {
        blazes.forEachIndexed { i, entity ->
            val color = when (i) {
                0 -> Color(0, 1, 0)
                1 -> Color(1, 5, 0)
                else -> Color(1, 1, 1)
            }

            RenderUtils.drawBoxOutline(entity.posX, entity.posY - 2, entity.posZ, 0.5, color,)

            // Drawing lines between the blazes
            //if (Config.blazeSolverNextLine && i > 0 && i <= Config.blazeSolverLines) {

            RenderUtils.draw3DLine(blazes[i - 1].positionVector, entity.positionVector, color, 1, false)

        }
    }

}
