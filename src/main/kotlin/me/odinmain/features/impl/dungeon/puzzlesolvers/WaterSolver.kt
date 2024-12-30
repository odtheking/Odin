package me.odinmain.features.impl.dungeon.puzzlesolvers

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import me.odinmain.utils.equal
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.Renderer
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.skyblock.dungeon.DungeonUtils.getRealCoords
import me.odinmain.utils.skyblock.getBlockAt
import me.odinmain.utils.skyblock.modMessage
import me.odinmain.utils.toVec3
import net.minecraft.init.Blocks
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.util.BlockPos
import net.minecraft.util.Vec3
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.util.*
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set

object WaterSolver {

    private var waterSolutions: JsonObject

    init {
        val isr = WaterSolver::class.java.getResourceAsStream("/watertimes.json")?.let { InputStreamReader(it, StandardCharsets.UTF_8) }
        waterSolutions = JsonParser().parse(isr).asJsonObject
    }

    private var solutions = HashMap<LeverBlock, Array<Double>>()
    private var patternIdentifier = -1
    private var openedWater = -1L

    fun scan() = with (DungeonUtils.currentRoom) {
        if (this?.data?.name != "Water Board" || patternIdentifier != -1) return@with
        val extendedSlots = WoolColor.entries.joinToString("") { if (it.isExtended) it.ordinal.toString() else "" }.takeIf { it.length == 3 } ?: return

        patternIdentifier = when {
            getBlockAt(getRealCoords(14, 77, 27)) == Blocks.hardened_clay -> 0 // right block == clay
            getBlockAt(getRealCoords(16, 78, 27)) == Blocks.emerald_block -> 1 // left block == emerald
            getBlockAt(getRealCoords(14, 78, 27)) == Blocks.diamond_block -> 2 // right block == diamond
            getBlockAt(getRealCoords(14, 78, 27)) == Blocks.quartz_block  -> 3 // right block == quartz
            else -> return@with modMessage("§cFailed to get Water Board pattern.")
        }

        modMessage("$patternIdentifier || ${WoolColor.entries.filter { it.isExtended }.joinToString(", ") { it.name.lowercase() }}")

        solutions.clear()
        waterSolutions[patternIdentifier.toString()].asJsonObject[extendedSlots].asJsonObject.entrySet().forEach {
            solutions[
                when (it.key) {
                    "diamond_block" -> LeverBlock.DIAMOND
                    "emerald_block" -> LeverBlock.EMERALD
                    "hardened_clay" -> LeverBlock.CLAY
                    "quartz_block"  -> LeverBlock.QUARTZ
                    "gold_block"    -> LeverBlock.GOLD
                    "coal_block"    -> LeverBlock.COAL
                    "water"         -> LeverBlock.WATER
                    else -> LeverBlock.NONE
                }
            ] = it.value.asJsonArray.map { it.asDouble }.toTypedArray()
        }
    }

    fun onRenderWorld(showTracer: Boolean, tracerColorFirst: Color, tracerColorSecond: Color) {
        if (patternIdentifier == -1 || solutions.isEmpty() || DungeonUtils.currentRoomName != "Water Board") return

        val solutionList = solutions
            .flatMap { (lever, times) -> times.drop(lever.i).map { Pair(lever, it) } }
            .sortedBy { (lever, time) -> time + if (lever == LeverBlock.WATER) 0.01 else 0.0 }

        if (showTracer) {
            val firstSolution = solutionList.firstOrNull()?.first ?: return
            Renderer.drawTracer(firstSolution.leverPos.addVector(.5, .5, .5), color = tracerColorFirst, depth = true)

            if (solutionList.size > 1 && firstSolution.leverPos != solutionList[1].first.leverPos) {
                Renderer.draw3DLine(
                    listOf(firstSolution.leverPos.addVector(.5, .5, .5), solutionList[1].first.leverPos.addVector(.5, .5, .5)),
                    color = tracerColorSecond, lineWidth = 1.5f, depth = true
                )
            }
        }

        solutions.forEach { (lever, times) ->
            times.drop(lever.i).forEachIndexed { index, time ->
                Renderer.drawStringInWorld(when {
                    openedWater == -1L && time == 0.0 -> "§a§lCLICK ME!"
                    openedWater == -1L -> "§e${time}s"
                    else ->
                        (openedWater + time * 1000L - System.currentTimeMillis()).takeIf { it > 0 }?.let { "§e${String.format(Locale.US, "%.2f", it / 1000)}s" } ?: "§a§lCLICK ME!"
                }, lever.leverPos.addVector(0.5, (index + lever.i) * 0.5 + 1.5, 0.5), Color.WHITE, scale = 0.04f)
            }
        }
    }

    fun waterInteract(event: C08PacketPlayerBlockPlacement) {
        if (solutions.isEmpty()) return
        LeverBlock.entries.find { it.leverPos.equal(event.position.toVec3()) }?.let {
            if (it == LeverBlock.WATER && openedWater == -1L) openedWater = System.currentTimeMillis()
            it.i++
        }
    }

    fun reset() {
        LeverBlock.entries.forEach { it.i = 0 }
        patternIdentifier = -1
        solutions.clear()
        openedWater = -1
    }

    private enum class WoolColor(val relativePosition: BlockPos) {
        PURPLE(BlockPos(15, 56, 19)),
        ORANGE(BlockPos(15, 56, 18)),
        BLUE(BlockPos(15, 56, 17)),
        GREEN(BlockPos(15, 56, 16)),
        RED(BlockPos(15, 56, 15));

        val isExtended: Boolean get() =
            DungeonUtils.currentRoom?.let { getBlockAt(it.getRealCoords(relativePosition)) == Blocks.wool } == true
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