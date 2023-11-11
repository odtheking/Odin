package me.odinmain.features.impl.dungeon

import me.odinmain.OdinMain.mc
import me.odinmain.features.impl.dungeon.PuzzleSolvers.mazeColorMultiple
import me.odinmain.features.impl.dungeon.PuzzleSolvers.mazeColorOne
import me.odinmain.features.impl.dungeon.PuzzleSolvers.tpMaze
import me.odinmain.ui.clickgui.util.ColorUtil.withAlpha
import me.odinmain.utils.VecUtils
import me.odinmain.utils.VecUtils.toAABB
import me.odinmain.utils.render.world.RenderUtils
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import net.minecraft.init.Blocks
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraft.util.Vec3

object TPMaze {
    var portals = setOf<BlockPos>()
    var correctPortals = listOf<BlockPos>()


    fun scan() {
        if (!tpMaze || portals.size >= 30 || DungeonUtils.currentRoomName != "Teleport Maze") return
        val pos = mc.thePlayer?.position ?: return
        portals = portals.plus(
            BlockPos.getAllInBox(
                BlockPos(pos.x + 22, 70, pos.z + 22),
                BlockPos(pos.x - 22, 69, pos.z - 22)
            ).filter {
                mc.theWorld.getBlockState(it).block == Blocks.end_portal_frame
            })
    }

    fun tpPacket(event: S08PacketPlayerPosLook) {
        if (DungeonUtils.currentRoomName != "Teleport Maze") return
        getCorrectPortals(Vec3(event.x, event.y, event.z), event.yaw, event.pitch)
    }

    fun getCorrectPortals(pos: Vec3, yaw: Float, pitch: Float) {
        if (correctPortals.isEmpty()) correctPortals = correctPortals.plus(portals)

        correctPortals = correctPortals.filter {
            VecUtils.isXZInterceptable(
                AxisAlignedBB(it.x.toDouble(), it.y.toDouble(), it.z.toDouble(), it.x + 1.0, it.y + 4.0, it.z + 1.0).expand(0.75, 0.0, 0.75),
                80f,
                pos,
                yaw,
                pitch
            ) && !it.toAABB().expand(.5, .0, .5).isVecInside(mc.thePlayer.positionVector)
        }
    }


    fun tpRender() {
        if (DungeonUtils.currentRoomName != "Teleport Maze" || !tpMaze) return
        val color = if (correctPortals.size == 1) mazeColorOne else mazeColorMultiple
        correctPortals.forEach {
            RenderUtils.drawFilledBox(it.toAABB(), color.withAlpha(.5f), phase = true)
        }
    }
}