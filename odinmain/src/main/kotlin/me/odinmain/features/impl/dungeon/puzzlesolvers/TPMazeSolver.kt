package me.odinmain.features.impl.dungeon.puzzlesolvers

import me.odinmain.OdinMain.mc
import me.odinmain.events.impl.DungeonEvents
import me.odinmain.features.impl.dungeon.puzzlesolvers.PuzzleSolvers.mazeColorMultiple
import me.odinmain.features.impl.dungeon.puzzlesolvers.PuzzleSolvers.mazeColorOne
import me.odinmain.features.impl.dungeon.puzzlesolvers.PuzzleSolvers.mazeColorVisited
import me.odinmain.ui.clickgui.util.ColorUtil.withAlpha
import me.odinmain.utils.*
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.Renderer
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.skyblock.dungeon.DungeonUtils.getRealCoords
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
        val room = event.room ?: return
        if (room.data.name != "Teleport Maze") return

        tpPads = BlockPos.getAllInBox(room.getRealCoords(BlockPos(0, 69, 0)), room.getRealCoords(BlockPos(30, 69, 30)))
            .filter { getBlockAt(it) == Blocks.end_portal_frame }.toSet()
    }

    fun tpPacket(event: S08PacketPlayerPosLook) {
        if (DungeonUtils.currentRoomName != "Teleport Maze" || event.x % 0.5 != 0.0 || event.y != 69.5 || event.z % 0.5 != 0.0 || tpPads.isEmpty()) return
        visited.addAll(tpPads.filter { Vec3(event.x, event.y, event.z).toAABB().expand(0.5, 0.0, 0.5).intersectsWith(it.toAABB()) ||
                mc.thePlayer?.entityBoundingBox?.expand(0.5, 0.0, 0.5)?.intersectsWith(it.toAABB()) == true })
        getCorrectPortals(Vec3(event.x, event.y, event.z), event.yaw, event.pitch)
    }

    private fun getCorrectPortals(pos: Vec3, yaw: Float, pitch: Float) {
        if (correctPortals.isEmpty()) correctPortals = correctPortals.plus(tpPads)

        correctPortals = correctPortals.filter {
            isXZInterceptable(
                AxisAlignedBB(it.x.toDouble(), it.y.toDouble(), it.z.toDouble(), it.x + 1.0, it.y + 4.0, it.z + 1.0).expand(0.75, 0.0, 0.75),
                60f, pos, yaw, pitch
            ) && !it.toAABB().expand(.5, .0, .5).isVecInside(mc.thePlayer?.positionVector)
        }
    }

    fun tpRender() {
        if (DungeonUtils.currentRoomName != "Teleport Maze") return
        val color = if (correctPortals.size == 1) mazeColorOne else mazeColorMultiple
        tpPads.forEach {
            when (it) {
                in visited -> Renderer.drawBlock(it, mazeColorVisited, outlineAlpha = 0, fillAlpha = mazeColorVisited.alpha, depth = true)
                in correctPortals -> Renderer.drawBlock(it, color, outlineAlpha = 0, fillAlpha = color.alpha, depth = false)
                else -> Renderer.drawBlock(it, Color.WHITE.withAlpha(0.5f), outlineAlpha = 0, fillAlpha = 0.5f, depth = true)
            }
        }
    }

    fun reset() {
        correctPortals = listOf()
        visited = CopyOnWriteArraySet<BlockPos>()
    }
}