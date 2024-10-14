package me.odinmain.features.impl.dungeon.puzzlesolvers

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import me.odinmain.OdinMain.mc
import me.odinmain.features.impl.dungeon.puzzlesolvers.PuzzleSolvers.showOrder
import me.odinmain.utils.Vec2
import me.odinmain.utils.addRotationCoords
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.RenderUtils.renderVec
import me.odinmain.utils.render.Renderer
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.skyblock.dungeon.tiles.Room
import me.odinmain.utils.skyblock.dungeon.tiles.Rotations
import me.odinmain.utils.skyblock.getBlockAt
import me.odinmain.utils.skyblock.modMessage
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

    private var chestPosition: Vec2 = Vec2(0, 0)
    private var roomFacing: Rotations = Rotations.NONE
    private var variant = -1
    private var extendedSlots = ""
    private var solutions = mutableMapOf<LeverBlock, Array<Double>>()
    private var openedWater = -1L

    fun scan() {
        val room = DungeonUtils.currentFullRoom?.room ?: return
        if (room.data.name != "Water Board" || variant != -1) return
        solve(room)
    }

    private fun solve(room: Room) {
        roomFacing = room.rotation
        chestPosition = room.vec2.addRotationCoords(roomFacing, -7)

        val pistonHeadPosition = chestPosition.addRotationCoords(roomFacing, -5).let { BlockPos(it.x, 82, it.z) }
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
            else -> -1
        }

        extendedSlots = ""
        WoolColor.entries.forEach { if (it.isExtended) extendedSlots += it.ordinal.toString() }

        // If the extendedSlots length is not 3, then retry.
        if (extendedSlots.length != 3) {
            extendedSlots = ""
            variant = -1
            return
        }

        // Print the variant and extendedSlots.
        modMessage("Variant: $variant:$extendedSlots:${roomFacing.name}")

        // Clear the solutions and add the new solutions.
        solutions.clear()
        val solutionObj = waterSolutions[variant.toString()].asJsonObject[extendedSlots].asJsonObject
        for (mutableEntry in solutionObj.entrySet()) {
            solutions[
                when (mutableEntry.key) {
                    "minecraft:quartz_block" -> LeverBlock.QUARTZ
                    "minecraft:gold_block" -> LeverBlock.GOLD
                    "minecraft:coal_block" -> LeverBlock.COAL
                    "minecraft:diamond_block" -> LeverBlock.DIAMOND
                    "minecraft:emerald_block" -> LeverBlock.EMERALD
                    "minecraft:hardened_clay" -> LeverBlock.CLAY
                    "minecraft:water" -> LeverBlock.WATER
                    else -> LeverBlock.NONE
                }
            ] = mutableEntry.value.asJsonArray.map { it.asDouble }.toTypedArray()
        }
    }

    fun waterRender() {
        if (DungeonUtils.currentRoomName != "Water Board" || variant == -1) return

        val solutions = ConcurrentHashMap(solutions)

        val solutionList = solutions
            .flatMap { (lever, times) -> times.drop(lever.i).map { Pair(lever, it) } }
            .sortedBy { (lever, time) -> time + if (lever == LeverBlock.WATER) 0.01 else 0.0 }

        val sortedSolutions = solutions.flatMap { (lever, times) ->
            times.drop(lever.i).filter { it != 0.0 }
        }.sorted()

        val first = solutionList.firstOrNull() ?: return

        if (PuzzleSolvers.showTracer) Renderer.draw3DLine(mc.thePlayer.renderVec, Vec3(first.first.leverPos).addVector(.5, .5, .5), color = PuzzleSolvers.tracerColorFirst, depth = true)

        if (solutionList.size > 1 && PuzzleSolvers.showTracer) {
            if (first.first.leverPos != solutionList[1].first.leverPos) {
                Renderer.draw3DLine(
                    Vec3(solutionList.first().first.leverPos).addVector(0.5, 0.5, 0.5),
                    Vec3(solutionList[1].first.leverPos).addVector(0.5, 0.5, 0.5),
                    color = PuzzleSolvers.tracerColorSecond,
                    lineWidth = 1.5f,
                    depth = true
                )
            }
        }

        for (solution in solutions) {
            var orderText = ""
            solution.value.drop(solution.key.i).forEach {
                orderText = if (it == 0.0) orderText.plus("0")
                else orderText.plus("${if (orderText.isEmpty()) "" else ", "}${sortedSolutions.indexOf(it) + 1}")
            }
            if (showOrder)
                Renderer.drawStringInWorld(orderText, Vec3(solution.key.leverPos).addVector(.5, .5, .5), Color.WHITE, scale = .035f)

            for (i in solution.key.i until solution.value.size) {
                val time = solution.value[i]
                val displayText = if (openedWater == -1L) {
                    if (time == 0.0) "§a§lCLICK ME!"
                    else "§e${time}s"
                } else {
                    val remainingTime = openedWater + time * 1000L - System.currentTimeMillis()
                    if (remainingTime > 0) "§e${String.format(Locale.US, "%.2f",remainingTime / 1000)}s"
                    else "§a§lCLICK ME!"
                }

                Renderer.drawStringInWorld(displayText, Vec3(solution.key.leverPos).addVector(0.5, (i - solution.key.i) * 0.5 + 1.5, 0.5), Color.WHITE, scale = 0.04f)
            }
        }
    }

    fun waterInteract(event: C08PacketPlayerBlockPlacement) {
        if (solutions.isEmpty()) return
        LeverBlock.entries.find { it.leverPos == event.position }?.let {
            it.i++
            if (it != LeverBlock.WATER || openedWater != -1L) return
            openedWater = System.currentTimeMillis()
        }
    }

    fun reset() {
        chestPosition = Vec2(0, 0)
        roomFacing = Rotations.NONE
        variant = -1
        extendedSlots = ""
        solutions.clear()
        openedWater = -1
        LeverBlock.entries.forEach { it.i = 0 }
    }

    enum class WoolColor {
        PURPLE, ORANGE, BLUE, GREEN, RED;

        val isExtended: Boolean
            get() = chestPosition.addRotationCoords(roomFacing, 3 + ordinal).let { getBlockAt(it.x, 56, it.z ) == Blocks.wool }
    }

    enum class LeverBlock(var i: Int = 0) {
        QUARTZ, GOLD, COAL, DIAMOND, EMERALD, CLAY, WATER, NONE;

        val leverPos: BlockPos
            get() {
                return if (this == WATER)
                    chestPosition.addRotationCoords(roomFacing, 17).let { BlockPos(it.x, 60, it.z) }
                else
                    chestPosition.addRotationCoords(if (ordinal < 3) roomFacing.rotateY() else roomFacing.rotateYCCW(), 5)
                        .addRotationCoords(roomFacing, (ordinal % 3 * 5) + 2).let { BlockPos(it.x, 61, it.z) }
            }

    }
}