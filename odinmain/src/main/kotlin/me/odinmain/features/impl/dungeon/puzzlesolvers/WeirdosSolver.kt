package me.odinmain.features.impl.dungeon.puzzlesolvers

import me.odinmain.OdinMain.mc
import me.odinmain.utils.*
import me.odinmain.utils.render.Renderer
import me.odinmain.utils.skyblock.PlayerUtils
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.util.Vec3

object WeirdosSolver {
    private var correctPos: Vec3? = null
    private var wrongPositions = mutableListOf<Vec3>()

    fun onNPCMessage(npc: String, msg: String) {
        if (solutions.none { it.matches(msg) } && wrong.none { it.matches(msg) }) return
        val correctNPC = mc.theWorld?.loadedEntityList?.filterIsInstance<EntityArmorStand>()?.find { it.name.noControlCodes == npc } ?: return
        val room = DungeonUtils.currentFullRoom?.room ?: return
        val pos = Vec3(correctNPC.posX - 0.5, 69.0, correctNPC.posZ - 0.5).addRotationCoords(room.rotation, -1, 0)

        if (solutions.any {it.matches(msg) }) {
            correctPos = pos
            PlayerUtils.playLoudSound("note.pling", 2f, 1f)
        }
        else wrongPositions.add(pos)
    }

    fun onRenderWorld() {
        if (DungeonUtils.currentRoomName != "Three Weirdos") return
        correctPos?.let {
            Renderer.drawBox(it.toAABB(), PuzzleSolvers.weirdosColor, outlineAlpha = if (PuzzleSolvers.weirdosStyle == 0) 0 else PuzzleSolvers.weirdosColor.alpha,
                fillAlpha = if (PuzzleSolvers.weirdosStyle == 1) 0 else PuzzleSolvers.weirdosColor.alpha)
        }
        wrongPositions.forEach {
            Renderer.drawBox(it.toAABB(), PuzzleSolvers.weirdosWrongColor, outlineAlpha = if (PuzzleSolvers.weirdosStyle == 0) 0 else PuzzleSolvers.weirdosWrongColor.alpha,
                fillAlpha = if (PuzzleSolvers.weirdosStyle == 1) 0 else PuzzleSolvers.weirdosWrongColor.alpha)
        }
    }

    fun reset() {
        correctPos = null
        wrongPositions.clear()
    }

    private val solutions = listOf(
        Regex("The reward is not in my chest!"),
        Regex("At least one of them is lying, and the reward is not in .+'s chest.?"),
        Regex("My chest doesn't have the reward. We are all telling the truth.?"),
        Regex("My chest has the reward and I'm telling the truth!"),
        Regex("The reward isn't in any of our chests.?"),
        Regex("Both of them are telling the truth. Also, .+ has the reward in their chest.?"),
    )

    private val wrong = listOf(
        Regex("One of us is telling the truth!"),
        Regex("They are both telling the truth. The reward isn't in .+'s chest."),
        Regex("We are all telling the truth!"),
        Regex(".+ is telling the truth and the reward is in his chest."),
        Regex("My chest doesn't have the reward. At least one of the others is telling the truth!"),
        Regex("One of the others is lying."),
        Regex("They are both telling the truth, the reward is in .+'s chest."),
        Regex("They are both lying, the reward is in my chest!"),
        Regex("The reward is in my chest."),
        Regex("The reward is not in my chest. They are both lying."),
        Regex(".+ is telling the truth."),
        Regex("My chest has the reward.")
    )
}