package me.odinmain.features.impl.dungeon.puzzlesolvers

import me.odinmain.OdinMain.mc
import me.odinmain.utils.addRotationCoords
import me.odinmain.utils.noControlCodes
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.Renderer
import me.odinmain.utils.skyblock.PlayerUtils
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.util.BlockPos
import java.util.concurrent.CopyOnWriteArraySet

object WeirdosSolver {
    private var correctPos: BlockPos? = null
    private var wrongPositions = CopyOnWriteArraySet<BlockPos>()

    fun onNPCMessage(npc: String, msg: String) {
        if (solutions.none { it.matches(msg) } && wrong.none { it.matches(msg) }) return
        val correctNPC = mc.theWorld?.loadedEntityList?.find { it is EntityArmorStand && it.name.noControlCodes == npc } ?: return
        val room = DungeonUtils.currentRoom ?: return
        val pos = BlockPos(correctNPC.posX - 0.5, 69.0, correctNPC.posZ - 0.5).addRotationCoords(room.rotation, -1, 0)

        if (solutions.any { it.matches(msg) }) {
            correctPos = pos
            PlayerUtils.playLoudSound("note.pling", 2f, 1f)
        } else wrongPositions.add(pos)
    }

    fun onRenderWorld(weirdosColor: Color, weirdosWrongColor: Color, weirdosStyle: Int) {
        if (DungeonUtils.currentRoomName != "Three Weirdos") return
        correctPos?.let { Renderer.drawStyledBlock(it, weirdosColor, weirdosStyle) }
        wrongPositions.forEach {
            Renderer.drawStyledBlock(it, weirdosWrongColor, weirdosStyle)
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