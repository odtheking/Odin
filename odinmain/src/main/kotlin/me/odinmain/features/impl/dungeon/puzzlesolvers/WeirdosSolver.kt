package me.odinmain.features.impl.dungeon.puzzlesolvers

import me.odinmain.OdinMain.mc
import me.odinmain.utils.*
import me.odinmain.utils.render.Renderer
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.util.Vec3

object WeirdosSolver {
    private var correctPos: Vec3? = null

    fun onNPCMessage(npc: String, msg: String) {
        if (solutions.none { it.matches(msg) }) return
        val correctNPC = mc.theWorld.loadedEntityList.filterIsInstance<EntityArmorStand>().find { it.name.noControlCodes == npc } ?: return
        val room = DungeonUtils.currentRoom?.room ?: return

        correctPos = Vec3(correctNPC.posX - 0.5, 69.0, correctNPC.posZ - 0.5).addRotationCoords(room.rotation, -1, 0)
    }

    fun onRenderWorld() {
        if (DungeonUtils.currentRoomName != "Three Weirdos") return
        correctPos?.let {
            Renderer.drawBox(it.toAABB(), PuzzleSolvers.weirdosColor, outlineAlpha = if (PuzzleSolvers.weirdosStyle == 0) 0 else PuzzleSolvers.weirdosColor.alpha, fillAlpha = if (PuzzleSolvers.weirdosStyle == 1) 0 else PuzzleSolvers.weirdosColor.alpha)
        }
    }

    fun weirdosReset() {
        correctPos = null
    }

    private val solutions = listOf(
        Regex("The reward is not in my chest!"),
        Regex("At least one of them is lying, and the reward is not in .+'s chest.?"),
        Regex("My chest doesn't have the reward. We are all telling the truth.?"),
        Regex("My chest has the reward and I'm telling the truth!"),
        Regex("The reward isn't in any of our chests.?"),
        Regex("Both of them are telling the truth. Also, .+ has the reward in their chest.?"),
    )
}