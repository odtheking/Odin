package com.odtheking.odin.features.impl.dungeon.puzzlesolvers

import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.events.RenderEvent
import com.odtheking.odin.events.RoomEnterEvent
import com.odtheking.odin.utils.Color
import com.odtheking.odin.utils.JsonResourceLoader
import com.odtheking.odin.utils.render.drawStyledBox
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import net.minecraft.core.BlockPos
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket
import net.minecraft.world.phys.AABB

object BoulderSolver {
    private data class BoxPosition(val render: AABB, val click: BlockPos)
    private var currentPositions = mutableListOf<BoxPosition>()
    private var solutions: Map<String, List<List<Int>>> = JsonResourceLoader.loadJson(
        "/assets/odin/puzzles/boulderSolutions.json", emptyMap()
    )

    fun onRoomEnter(event: RoomEnterEvent) = with(event.room) {
        if (this?.data?.name != "Boulder") return@with reset()
        var str = ""
        for (z in 24 downTo 9 step 3) {
            for (x in 24 downTo 6 step 3) {
                str += if (mc.level?.getBlockState(getRealCoords(BlockPos(x, 66, z)))?.isAir == true) "0" else "1"
            }
        }
        currentPositions = solutions[str]?.map { sol ->
            val render = getRealCoords(BlockPos(sol[0], 65, sol[1]))
            val click = getRealCoords(BlockPos(sol[2], 65, sol[3]))
            BoxPosition(AABB(render), click)
        }?.toMutableList() ?: return@with
    }

    fun onRenderWorld(event: RenderEvent.Extract, showAllBoulderClicks: Boolean, boulderStyle: Int, boulderColor: Color) {
        if (DungeonUtils.currentRoomName != "Boulder" || currentPositions.isEmpty()) return
        if (showAllBoulderClicks) currentPositions.forEach {
            event.drawStyledBox(it.render, boulderColor, boulderStyle, false)
        } else currentPositions.firstOrNull()?.let {
            event.drawStyledBox(it.render, boulderColor, boulderStyle, false)
        }
    }

    fun playerInteract(event: ServerboundUseItemOnPacket) {
        currentPositions.remove(currentPositions.firstOrNull { it.click == event.hitResult.blockPos })
    }

    fun reset() {
        currentPositions = mutableListOf()
    }
}