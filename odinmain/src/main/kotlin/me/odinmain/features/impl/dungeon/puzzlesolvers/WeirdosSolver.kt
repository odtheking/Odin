package me.odinmain.features.impl.dungeon.puzzlesolvers

import me.odinmain.OdinMain.mc
import me.odinmain.events.impl.EnteredDungeonRoomEvent
import me.odinmain.utils.addRotationCoords
import me.odinmain.utils.noControlCodes
import me.odinmain.utils.render.RenderUtils
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.util.BlockPos

object WeirdosSolver {
    private val solutions = listOf(
        Regex("The reward is not in my chest!"),
        Regex("At least one of them is lying, and the reward is not in .+'s chest.?"),
        Regex("My chest doesn't have the reward. We are all telling the truth.?"),
        Regex("My chest has the reward and I'm telling the truth!"),
        Regex("The reward isn't in any of our chests.?"),
        Regex("Both of them are telling the truth. Also, .+ has the reward in their chest.?"),
    )

    private var correctPos: BlockPos? = null

    fun onNPCMessage(npc: String, msg: String) {
        if (solutions.none { it.matches(msg) }) return
        val correctNPC = mc.theWorld.loadedEntityList.filterIsInstance<EntityArmorStand>().find { it.name.noControlCodes == npc } ?: return
        val room = DungeonUtils.currentRoom?.room ?: return

        correctPos = BlockPos(correctNPC.posX, 69.0, correctNPC.posZ).addRotationCoords(room.rotation, -1, 0)
    }

    fun onRenderWorld() {
        if (DungeonUtils.currentRoomName != "Three Weirdos") return
        correctPos?.let {
            RenderUtils.drawBlockBox(it, PuzzleSolvers.weirdosColor)
        }
    }

    fun weirdosRoomEnter(event: EnteredDungeonRoomEvent) {
        if (event.room?.room?.data?.name != "Three Weirdos")
            correctPos = null
    }
}