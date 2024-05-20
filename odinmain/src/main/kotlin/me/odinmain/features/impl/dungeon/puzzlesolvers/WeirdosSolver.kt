package me.odinmain.features.impl.dungeon.puzzlesolvers

import me.odinmain.OdinMain.mc
import me.odinmain.events.impl.EnteredDungeonRoomEvent
import me.odinmain.utils.Vec2
import me.odinmain.utils.addRotationCoords
import me.odinmain.utils.floor
import me.odinmain.utils.noControlCodes
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.RenderUtils
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.skyblock.modMessage
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.util.BlockPos
import net.minecraft.util.Vec3

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

        val vec = Vec3(correctNPC.posX, 69.0, correctNPC.posZ).addRotationCoords(room.rotation, -1, 0)
        modMessage(BlockPos(vec))
        correctPos = BlockPos(vec)
    }

    fun onRenderWorld() {
        correctPos?.let {
            RenderUtils.drawBlockBox(it, Color.WHITE)
        }
    }

    fun enteredRoom(event: EnteredDungeonRoomEvent) {

    }

}