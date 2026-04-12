package com.odtheking.odin.features.impl.dungeon.puzzlesolvers

import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.events.RenderEvent
import com.odtheking.odin.events.RoomEnterEvent
import com.odtheking.odin.utils.Color
import com.odtheking.odin.utils.Color.Companion.withAlpha
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.getBlockBounds
import com.odtheking.odin.utils.isXZInterceptable
import com.odtheking.odin.utils.render.drawFilledBox
import com.odtheking.odin.utils.render.drawTracer
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils.getRealCoords
import net.minecraft.core.BlockPos
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket
import net.minecraft.util.Mth
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import java.util.concurrent.CopyOnWriteArraySet
import kotlin.math.abs
import kotlin.math.atan2

object TPMazeSolver {
    private var tpPads = listOf<BlockPos>()
    private var correctPortals = listOf<BlockPos>()
    private var visited = CopyOnWriteArraySet<BlockPos>()
    private var best: BlockPos? = null

    fun onRoomEnter(event: RoomEnterEvent) = with(event.room) {
        if (this?.data?.name == "Teleport Maze") tpPads = endPortalFrameLocations.map { getRealCoords(BlockPos(it.x, it.y, it.z)) }
    }

    fun tpPacket(event: ClientboundPlayerPositionPacket) {
        val pos = event.change.position
        if (DungeonUtils.currentRoomName != "Teleport Maze" || pos.x % 0.5 != 0.0 || pos.y != 69.5 || pos.z % 0.5 != 0.0 || tpPads.isEmpty()) return

        val posAABB = AABB.unitCubeFromLowerCorner(pos).inflate(1.0, 0.0, 1.0)
        visited.addAll(tpPads.filter { posAABB.intersects(AABB(it)) || mc.player?.boundingBox?.inflate(1.0, 0.0, 1.0)?.intersects(AABB(it)) == true })
        getCorrectPortals(pos, event.change.yRot, event.change.xRot)

        val currentPad = tpPads.firstOrNull { posAABB.intersects(AABB(it)) } ?: return
        val index = tpPads.indexOf(currentPad)

        if (index in 28..29) { best = null; return }

        val groupStart = index / 4 * 4
        if (groupStart + 4 > tpPads.size) return

        val candidates = tpPads.slice(groupStart until groupStart + 4).filter { it != currentPad && it !in visited }

        best = candidates.firstOrNull { it in correctPortals }
            ?: candidates.minByOrNull {
                val yaw = (atan2(it.center.z - pos.z, it.center.x - pos.x) * 180.0 / Math.PI).toFloat() - 90f
                abs(Mth.wrapDegrees(yaw) - Mth.wrapDegrees(event.change.yRot))
            }
    }

    private fun getCorrectPortals(pos: Vec3, yaw: Float, pitch: Float) {
        if (correctPortals.isEmpty()) correctPortals = tpPads

        correctPortals = correctPortals.filter {
            it !in visited &&
            isXZInterceptable(
                AABB(it.x.toDouble(), it.y.toDouble(), it.z.toDouble(), it.x + 1.0, it.y + 4.0, it.z + 1.0).inflate(0.75, 0.0, 0.75),
                32.0, pos, yaw, pitch
            ) && !AABB(it).inflate(.5, .0, .5).intersects(mc.player?.boundingBox ?: return@filter false)
        }
    }

    fun onRenderWorld(event: RenderEvent.Extract, mazeColorOne: Color, mazeColorMultiple: Color, mazeColorVisited: Color, showTracer: Boolean, tracerColor: Color) {
        if (DungeonUtils.currentRoomName != "Teleport Maze") return
        tpPads.forEach {
            val aabb = it.getBlockBounds()?.move(it) ?: AABB(it)
            when (it) {
                in correctPortals -> event.drawFilledBox(aabb, if (correctPortals.size == 1) mazeColorOne else mazeColorMultiple, false)
                in visited -> event.drawFilledBox(aabb, mazeColorVisited, true)
                else -> event.drawFilledBox(aabb, Colors.WHITE.withAlpha(0.5f), true)
            }
        }

        if (showTracer) {
            val target = best ?: return
            event.drawTracer(Vec3(target.x + 0.5, target.y + 0.8, target.z + 0.5), tracerColor, false)
        }
    }

    fun reset() {
        correctPortals = listOf()
        visited = CopyOnWriteArraySet()
        best = null
    }

    private val endPortalFrameLocations = listOf(
        BlockPos(4, 69, 12), BlockPos(4, 69, 6),   BlockPos(10, 69, 12), BlockPos(10, 69, 6),
        BlockPos(4, 69, 20), BlockPos(4, 69, 14),  BlockPos(10, 69, 20), BlockPos(10, 69, 14),
        BlockPos(4, 69, 28), BlockPos(4, 69, 22),  BlockPos(10, 69, 28), BlockPos(10, 69, 22),
        BlockPos(12, 69, 28), BlockPos(12, 69, 22), BlockPos(18, 69, 28), BlockPos(18, 69, 22),
        BlockPos(20, 69, 28), BlockPos(20, 69, 22), BlockPos(26, 69, 28), BlockPos(26, 69, 22),
        BlockPos(26, 69, 20), BlockPos(26, 69, 14), BlockPos(20, 69, 20), BlockPos(20, 69, 14),
        BlockPos(26, 69, 12), BlockPos(26, 69, 6),  BlockPos(20, 69, 12), BlockPos(20, 69, 6),
        BlockPos(15, 69, 14), BlockPos(15, 69, 12)
    )
}