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
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import net.minecraft.util.*
import java.util.concurrent.CopyOnWriteArraySet

object TPMazeSolver {
    private var tpPads = setOf<BlockPos>()
    private var correctPortals = listOf<BlockPos>()
    private var visited = CopyOnWriteArraySet<BlockPos>()

    fun onRoomEnter(event: DungeonEvents.RoomEnterEvent) = with(event.room) {
        if (this?.data?.name == "Teleport Maze") tpPads = endPortalFrameLocations.map { getRealCoords(it.x, it.y, it.z) }.toSet()
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

    fun onRenderWorld() {
        if (DungeonUtils.currentRoomName != "Teleport Maze") return
        tpPads.forEach {
            when (it) {
                in correctPortals -> Renderer.drawBlock(it, if (correctPortals.size == 1) mazeColorOne else mazeColorMultiple, outlineAlpha = 0, depth = false)
                in visited -> Renderer.drawBlock(it, mazeColorVisited, outlineAlpha = 0, depth = true)
                else -> Renderer.drawBlock(it, Color.WHITE.withAlpha(0.5f), outlineAlpha = 0, fillAlpha = 0.5f, depth = true)
            }
        }
    }

    fun reset() {
        correctPortals = listOf()
        visited = CopyOnWriteArraySet<BlockPos>()
    }

    private val endPortalFrameLocations = setOf(
        BlockPos(-135, 0, -197), BlockPos(-129, 0, -197), BlockPos(-127, 0, -197),
        BlockPos(-121, 0, -197), BlockPos(-119, 0, -197), BlockPos(-113, 0, -197),
        BlockPos(-135, 0, -191), BlockPos(-129, 0, -191), BlockPos(-127, 0, -191),
        BlockPos(-121, 0, -191), BlockPos(-119, 0, -191), BlockPos(-113, 0, -191),
        BlockPos(-135, 0, -189), BlockPos(-129, 0, -189), BlockPos(-121, 0, -186),
        BlockPos(-119, 0, -186), BlockPos(-135, 0, -183), BlockPos(-129, 0, -183),
        BlockPos(-135, 0, -181), BlockPos(-129, 0, -181), BlockPos(-127, 0, -181),
        BlockPos(-121, 0, -181), BlockPos(-119, 0, -181), BlockPos(-113, 0, -181),
        BlockPos(-135, 0, -175), BlockPos(-129, 0, -175), BlockPos(-127, 0, -175),
        BlockPos(-121, 0, -175), BlockPos(-119, 0, -175), BlockPos(-113, 0, -175)
    )
}