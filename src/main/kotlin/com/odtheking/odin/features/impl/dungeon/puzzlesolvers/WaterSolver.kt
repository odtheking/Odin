package com.odtheking.odin.features.impl.dungeon.puzzlesolvers

import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.events.RenderEvent
import com.odtheking.odin.utils.Color
import com.odtheking.odin.utils.JsonResourceLoader
import com.odtheking.odin.utils.modMessage
import com.odtheking.odin.utils.render.drawLine
import com.odtheking.odin.utils.render.drawText
import com.odtheking.odin.utils.render.drawTracer
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils.getRealCoords
import com.odtheking.odin.utils.toFixed
import net.minecraft.core.BlockPos
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.phys.Vec3

object WaterSolver {

    private var waterSolutions: Map<String, Map<String, Map<String, Map<String, List<Double>>>>> = JsonResourceLoader.loadJson(
        "/assets/odin/puzzles/waterSolutions.json", emptyMap()
    )

    private var solutions = HashMap<LeverBlock, Array<Double>>()
    private var patternIdentifier = -1
    private var openedWaterTicks = -1
    private var tickCounter = 0

    fun scan(optimized: Boolean) = with (DungeonUtils.currentRoom) {
        if (this?.data?.name != "Water Board" || patternIdentifier != -1) return@with
        val extendedSlots = WoolColor.entries.joinToString("") { if (it.isExtended) it.ordinal.toString() else "" }.takeIf { it.length == 3 } ?: return@with

        patternIdentifier = when {
            mc.level?.getBlockState(getRealCoords(BlockPos(14, 77, 27)))?.block == Blocks.TERRACOTTA -> 0 // right block == clay
            mc.level?.getBlockState(getRealCoords(BlockPos(16, 78, 27)))?.block == Blocks.EMERALD_BLOCK -> 1 // left block == emerald
            mc.level?.getBlockState(getRealCoords(BlockPos(14, 78, 27)))?.block == Blocks.DIAMOND_BLOCK -> 2 // right block == diamond
            mc.level?.getBlockState(getRealCoords(BlockPos(14, 78, 27)))?.block == Blocks.QUARTZ_BLOCK  -> 3 // right block == quartz
            else -> return@with modMessage("§cFailed to get Water Board pattern. Was the puzzle already started?")
        }

        modMessage("$patternIdentifier || ${WoolColor.entries.filter { it.isExtended }.joinToString(", ") { it.name.lowercase() }}")

        solutions.clear()
        waterSolutions[optimized.toString()]?.get(patternIdentifier.toString())?.get(extendedSlots)?.forEach { (key, times) ->
            solutions[
                when (key) {
                    "diamond_block" -> LeverBlock.DIAMOND
                    "emerald_block" -> LeverBlock.EMERALD
                    "hardened_clay" -> LeverBlock.CLAY
                    "quartz_block"  -> LeverBlock.QUARTZ
                    "gold_block"    -> LeverBlock.GOLD
                    "coal_block"    -> LeverBlock.COAL
                    "water"         -> LeverBlock.WATER
                    else -> LeverBlock.NONE
                }
            ] = times.toTypedArray()
        }
    }

    fun onRenderWorld(event: RenderEvent.Extract, showTracer: Boolean, tracerColorFirst: Color, tracerColorSecond: Color) {
        if (patternIdentifier == -1 || solutions.isEmpty() || DungeonUtils.currentRoomName != "Water Board") return

        val solutionList = solutions
            .flatMap { (lever, times) -> times.drop(lever.i).map { Pair(lever, it) } }
            .sortedWith(
                compareBy(
                    { it.second != 0.0 },
                    { if (it.second == 0.0) it.first.ordinal else Int.MAX_VALUE },
                    { if (it.second != 0.0) it.second else 0.0 }
                )
            )
        if (showTracer) {
            val firstSolution = solutionList.firstOrNull()?.first ?: return
            mc.player?.let { event.drawTracer(Vec3(firstSolution.leverPos).add(.5, .5, .5), color = tracerColorFirst, depth = false) }

            if (solutionList.size > 1 && firstSolution.leverPos != solutionList[1].first.leverPos) {
                event.drawLine(
                    listOf(Vec3(firstSolution.leverPos).add(.5, .5, .5), Vec3(solutionList[1].first.leverPos).add(.5, .5, .5)),
                    color = tracerColorSecond, depth = false
                )
            }
        }

        solutions.forEach { (lever, times) ->
            times.drop(lever.i).forEachIndexed { index, time ->
                val timeInTicks = (time * 20).toInt()
                event.drawText(
                    when (openedWaterTicks) {
                        -1 if timeInTicks == 0 -> "§a§lCLICK ME!"
                        -1 -> "§e${time}s"
                        else -> (openedWaterTicks + timeInTicks - tickCounter).takeIf { it > 0 }?.let { "§e${(it / 20f).toFixed()}s" } ?: "§a§lCLICK ME!"
                    },
                    Vec3(lever.leverPos).add(0.5, (index + lever.i) * 0.5 + 1.5, 0.5),
                    scale = 1f, true
                )
            }
        }
    }

    fun waterInteract(event: ServerboundUseItemOnPacket) {
        if (solutions.isEmpty()) return
        LeverBlock.entries.find { it.leverPos == event.hitResult.blockPos }?.let {
            if (it == LeverBlock.WATER && openedWaterTicks == -1) openedWaterTicks = tickCounter
            it.i++
        }
    }

    fun onServerTick() {
        tickCounter++
    }

    fun reset() {
        LeverBlock.entries.forEach { it.i = 0 }
        patternIdentifier = -1
        solutions.clear()
        openedWaterTicks = -1
        tickCounter = 0
    }

    private enum class WoolColor(val relativePosition: BlockPos) {
        PURPLE(BlockPos(15, 56, 19)),
        ORANGE(BlockPos(15, 56, 18)),
        BLUE(BlockPos(15, 56, 17)),
        GREEN(BlockPos(15, 56, 16)),
        RED(BlockPos(15, 56, 15));

        inline val isExtended: Boolean get() =
            DungeonUtils.currentRoom?.let { mc.level?.getBlockState(it.getRealCoords(relativePosition))?.isAir == true } == false
    }

    private enum class LeverBlock(val relativePosition: BlockPos, var i: Int = 0) {
        COAL(BlockPos(20, 61, 10)),
        GOLD(BlockPos(20, 61, 15)),
        QUARTZ(BlockPos(20, 61, 20)),
        DIAMOND(BlockPos(10, 61, 20)),
        EMERALD(BlockPos(10, 61, 15)),
        CLAY(BlockPos(10, 61, 10)),
        WATER(BlockPos(15, 60, 5)),
        NONE(BlockPos(0, 0, 0));

        inline val leverPos: BlockPos
            get() = DungeonUtils.currentRoom?.getRealCoords(relativePosition) ?: BlockPos(0, 0, 0)
    }
}