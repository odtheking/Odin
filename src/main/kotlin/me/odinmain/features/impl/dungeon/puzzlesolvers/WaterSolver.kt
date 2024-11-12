package me.odinmain.features.impl.dungeon.puzzlesolvers

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import me.odinmain.utils.equal
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.Renderer
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.skyblock.dungeon.DungeonUtils.getRealCoords
import me.odinmain.utils.skyblock.dungeon.tiles.Room
import me.odinmain.utils.skyblock.getBlockAt
import me.odinmain.utils.skyblock.modMessage
import me.odinmain.utils.toVec3
import net.minecraft.init.Blocks
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.util.BlockPos
import net.minecraft.util.Vec3
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set

object WaterSolver {

    private var waterSolutions: JsonObject

    init {
        val isr = WaterSolver::class.java.getResourceAsStream("/watertimes.json")?.let { InputStreamReader(it, StandardCharsets.UTF_8) }
        waterSolutions = JsonParser().parse(isr).asJsonObject
    }

    private var variant = -1
    private var solutions = ConcurrentHashMap<LeverBlock, Array<Double>>()
    private var openedWater = -1L

    fun scan() = with (DungeonUtils.currentRoom) {
        if (this?.data?.name == "Water Board" && variant == -1) solve(this)
    }

    private fun solve(room: Room) {
        val extendedSlots = WoolColor.entries.joinToString("") { if (it.isExtended) it.ordinal.toString() else "" }.takeIf { it.length == 3 } ?: return

        val pistonHeadPosition = room.getRealCoords(BlockPos(15, 82, 27))
        val foundBlocks = mutableListOf(false, false, false, false, false)
        BlockPos.getAllInBox(BlockPos(pistonHeadPosition.x + 1, 78, pistonHeadPosition.z + 1), BlockPos(pistonHeadPosition.x - 1, 77, pistonHeadPosition.z - 1)).forEach {
            when (getBlockAt(it)) {
                Blocks.gold_block -> foundBlocks[0] = true
                Blocks.hardened_clay -> foundBlocks[1] = true
                Blocks.emerald_block -> foundBlocks[2] = true
                Blocks.quartz_block -> foundBlocks[3] = true
                Blocks.diamond_block -> foundBlocks[4] = true
            }
        }

        variant = when {
            foundBlocks[0] && foundBlocks[1] -> 0
            foundBlocks[2] && foundBlocks[3] -> 1
            foundBlocks[3] && foundBlocks[4] -> 2
            foundBlocks[0] && foundBlocks[3] -> 3
            else -> return
        }

        modMessage("variant: $variant, extended: $extendedSlots")

        solutions.clear()
        waterSolutions[variant.toString()].asJsonObject[extendedSlots].asJsonObject.entrySet().forEach {
            solutions[
                when (it.key) {
                    "quartz_block" -> LeverBlock.QUARTZ
                    "gold_block" -> LeverBlock.GOLD
                    "coal_block" -> LeverBlock.COAL
                    "diamond_block" -> LeverBlock.DIAMOND
                    "emerald_block" -> LeverBlock.EMERALD
                    "hardened_clay" -> LeverBlock.CLAY
                    "water" -> LeverBlock.WATER
                    else -> LeverBlock.NONE
                }
            ] = it.value.asJsonArray.map { it.asDouble }.toTypedArray()
        }
    }

    fun onRenderWorld() {
        if (variant == -1 || DungeonUtils.currentRoomName != "Water Board") return

        val solutionList = solutions
            .flatMap { (lever, times) -> times.drop(lever.i).map { Pair(lever, it) } }
            .sortedBy { (lever, time) -> time + if (lever == LeverBlock.WATER) 0.01 else 0.0 }

        val firstSolution = solutionList.firstOrNull() ?: return

        if (PuzzleSolvers.showTracer) Renderer.drawTracer(firstSolution.first.leverPos.addVector(.5, .5, .5), color = PuzzleSolvers.tracerColorFirst, depth = true)

        if (solutionList.size > 1 && PuzzleSolvers.showTracer && firstSolution.first.leverPos != solutionList[1].first.leverPos) {
            Renderer.draw3DLine(
                listOf(firstSolution.first.leverPos.addVector(.5, .5, .5), solutionList[1].first.leverPos.addVector(.5, .5, .5)),
                color = PuzzleSolvers.tracerColorSecond, lineWidth = 1.5f, depth = true
            )
        }

        solutions.forEach { (lever, times) ->
            times.drop(lever.i).forEachIndexed { index, time ->
                Renderer.drawStringInWorld(when {
                    openedWater == -1L && time == 0.0 -> "§a§lCLICK ME!"
                    openedWater == -1L -> "§e${time}s"
                    else -> {
                        val remainingTime = openedWater + time * 1000L - System.currentTimeMillis()
                        if (remainingTime > 0) "§e${String.format(Locale.US, "%.2f", remainingTime / 1000)}s" else "§a§lCLICK ME!"
                    }
                }, lever.leverPos.addVector(0.5, (index + lever.i) * 0.5 + 1.5, 0.5), Color.WHITE, scale = 0.04f)
            }
        }
    }

    fun waterInteract(event: C08PacketPlayerBlockPlacement) {
        if (solutions.isEmpty()) return
        LeverBlock.entries.find { it.leverPos.equal(event.position.toVec3()) }?.let {
            it.i++
            if (it == LeverBlock.WATER && openedWater == -1L) openedWater = System.currentTimeMillis()
        }
    }

    fun reset() {
        variant = -1
        solutions.clear()
        openedWater = -1
        LeverBlock.entries.forEach { it.i = 0 }
    }

    private enum class WoolColor(val relativePosition: BlockPos) {
        PURPLE(BlockPos(15, 56, 19)),
        ORANGE(BlockPos(15, 56, 18)),
        BLUE(BlockPos(15, 56, 17)),
        GREEN(BlockPos(15, 56, 16)),
        RED(BlockPos(15, 56, 15));

        val isExtended: Boolean get() =
            getBlockAt(DungeonUtils.currentRoom?.getRealCoords(relativePosition) ?: BlockPos(0, 0, 0)) == Blocks.wool
    }

    private enum class LeverBlock(val relativePosition: Vec3, var i: Int = 0) {
        QUARTZ(Vec3(20.0, 61.0, 20.0)),
        GOLD(Vec3(20.0, 61.0, 15.0)),
        COAL(Vec3(20.0, 61.0, 10.0)),
        DIAMOND(Vec3(10.0, 61.0, 20.0)),
        EMERALD(Vec3(10.0, 61.0, 15.0)),
        CLAY(Vec3(10.0, 61.0, 10.0)),
        WATER(Vec3(15.0, 60.0, 5.0)),
        NONE(Vec3(0.0, 0.0, 0.0));

        val leverPos: Vec3
            get() = DungeonUtils.currentRoom?.getRealCoords(relativePosition) ?: Vec3(0.0, 0.0, 0.0)
    }
}