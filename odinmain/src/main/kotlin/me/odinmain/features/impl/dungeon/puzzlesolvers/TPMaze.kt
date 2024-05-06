package me.odinmain.features.impl.dungeon.puzzlesolvers

import me.odinmain.OdinMain.mc
import me.odinmain.features.impl.dungeon.puzzlesolvers.PuzzleSolvers.mazeColorMultiple
import me.odinmain.features.impl.dungeon.puzzlesolvers.PuzzleSolvers.mazeColorOne
import me.odinmain.features.impl.dungeon.puzzlesolvers.PuzzleSolvers.mazeColorVisited
import me.odinmain.features.impl.dungeon.puzzlesolvers.PuzzleSolvers.solutionThroughWalls
import me.odinmain.utils.isXZInterceptable
import me.odinmain.utils.render.RenderUtils
import me.odinmain.utils.render.Renderer
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.toAABB
import net.minecraft.init.Blocks
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import net.minecraft.util.*
import java.util.concurrent.CopyOnWriteArraySet

object TPMaze {
    var portals = setOf<BlockPos>()
    var correctPortals = listOf<BlockPos>()
    private var visited = CopyOnWriteArraySet<BlockPos>()

    fun scan() {
        if (portals.size >= 30 || DungeonUtils.currentRoomName != "Teleport Maze") return
        val pos = mc.thePlayer?.position ?: return
        portals = portals.plus(
            BlockPos.getAllInBox(BlockPos(pos.x + 22, 70, pos.z + 22), BlockPos(pos.x - 22, 69, pos.z - 22))
                .filter { mc.theWorld.getBlockState(it).block == Blocks.end_portal_frame }
        )
    }

    fun tpPacket(event: S08PacketPlayerPosLook) {
        if (DungeonUtils.currentRoomName != "Teleport Maze") return
        val eventBlockPos = BlockPos(event.x, event.y, event.z).toAABB().expand(0.5, 0.0, 0.5)
        val playerBlockPos = mc.thePlayer.position.toAABB().expand(0.5, 0.0, 0.5)
        visited.addAll(
            portals.filter { eventBlockPos.intersectsWith(it.toAABB()) || playerBlockPos.intersectsWith(it.toAABB()) }
        )
        getCorrectPortals(Vec3(event.x, event.y, event.z), event.yaw, event.pitch)
    }

    fun getCorrectPortals(pos: Vec3, yaw: Float, pitch: Float) {
        if (correctPortals.isEmpty()) correctPortals = correctPortals.plus(portals)

        correctPortals = correctPortals.filter {
            isXZInterceptable(
                AxisAlignedBB(it.x.toDouble(), it.y.toDouble(), it.z.toDouble(), it.x + 1.0, it.y + 4.0, it.z + 1.0).expand(0.75, 0.0, 0.75),
                60f, pos, yaw, pitch
            ) && !it.toAABB().expand(.5, .0, .5).isVecInside(mc.thePlayer.positionVector)
        }
    }


    fun tpRender() {
        if (DungeonUtils.currentRoomName != "Teleport Maze") return
        val color = if (correctPortals.size == 1) mazeColorOne else mazeColorMultiple
        correctPortals.forEach {
            if (visited.contains(it) && correctPortals.size != 1) return@forEach
            Renderer.drawBox(RenderUtils.getBlockAABB(Blocks.end_portal_frame, it).expand(0.005, 0.005, 0.005), color, outlineAlpha = 0, fillAlpha = color.alpha, depth = !(solutionThroughWalls && correctPortals.size == 1))
        }
        visited.forEach {
            Renderer.drawBox(RenderUtils.getBlockAABB(Blocks.end_portal_frame, it).expand(0.005, 0.005, 0.005), mazeColorVisited, outlineAlpha = 0, fillAlpha = mazeColorVisited.alpha, depth = true)
        }
    }

    fun reset() {
        portals = setOf()
        correctPortals = listOf()
        visited = CopyOnWriteArraySet<BlockPos>()
    }
}