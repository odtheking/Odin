package me.odinmain.features.impl.dungeon.puzzlesolvers

import me.odinmain.OdinMain.mc
import me.odinmain.events.impl.DungeonEvents
import me.odinmain.features.impl.dungeon.puzzlesolvers.PuzzleSolvers.mazeColorMultiple
import me.odinmain.features.impl.dungeon.puzzlesolvers.PuzzleSolvers.mazeColorOne
import me.odinmain.features.impl.dungeon.puzzlesolvers.PuzzleSolvers.mazeColorVisited
import me.odinmain.features.impl.dungeon.puzzlesolvers.PuzzleSolvers.solutionThroughWalls
import me.odinmain.utils.*
import me.odinmain.utils.render.Renderer
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.skyblock.getBlockAt
import net.minecraft.init.Blocks
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import net.minecraft.util.*
import java.util.concurrent.CopyOnWriteArraySet

object TPMazeSolver {
    private var tpPads = setOf<BlockPos>()
    private var correctPortals = listOf<BlockPos>()
    private var visited = CopyOnWriteArraySet<BlockPos>()

    fun onRoomEnter(event: DungeonEvents.RoomEnterEvent) {
        val room = event.fullRoom?.room ?: return
        if (room.data.name != "Teleport Maze") return
        tpPads = setOf()
        BlockPos.getAllInBox(room.vec3.addRotationCoords(room.rotation, -16, -16).toBlockPos(), room.vec3.addRotationCoords(room.rotation, 16, 16).toBlockPos())
            .filter { getBlockAt(it) == Blocks.end_portal_frame }
            .forEach { tpPads = tpPads.plus(it) }
    }

    fun tpPacket(event: S08PacketPlayerPosLook) {
        if (DungeonUtils.currentRoomName != "Teleport Maze" || event.x % 0.5 != 0.0 || event.y != 69.5 || event.z % 0.5 != 0.0 || tpPads.isEmpty()) return
        visited.addAll(tpPads.filter { Vec3(event.x, event.y, event.z).toAABB().intersectsWith(it.toAABB()) || mc.thePlayer?.position?.toAABB()?.intersectsWith(it.toAABB()) == true })
        getCorrectPortals(Vec3(event.x, event.y, event.z), event.yaw, event.pitch)
    }

    private fun getCorrectPortals(pos: Vec3, yaw: Float, pitch: Float) {
        if (correctPortals.isEmpty()) correctPortals = correctPortals.plus(tpPads)

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
            Renderer.drawBlock(it, color, outlineAlpha = 0, fillAlpha = color.alpha, depth = !(solutionThroughWalls && correctPortals.size == 1))
        }
        visited.forEach {
            Renderer.drawBlock(it, mazeColorVisited, outlineAlpha = 0, fillAlpha = mazeColorVisited.alpha, depth = true)
        }
    }

    fun reset() {
        tpPads = setOf()
        correctPortals = listOf()
        visited = CopyOnWriteArraySet<BlockPos>()
    }
}