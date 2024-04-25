package me.odinmain.features.impl.dungeon.puzzlesolvers

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import me.odinmain.OdinMain.mc
import me.odinmain.features.impl.dungeon.puzzlesolvers.PuzzleSolvers.showOrder
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.Renderer
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.skyblock.modMessage
import net.minecraft.init.Blocks
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import net.minecraft.util.Vec3
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

object WaterSolver {

    private var waterSolutions: JsonObject

    init {
        val isr = WaterSolver::class.java.getResourceAsStream("/watertimes.json")
            ?.let { InputStreamReader(it, StandardCharsets.UTF_8) }
        waterSolutions = JsonParser().parse(isr).asJsonObject
    }

    private var chestPos: BlockPos? = null
    private var roomFacing: EnumFacing? = null
    private var prevInWaterRoom = false
    private var inWaterRoom = false
    private var variant = -1
    private var extendedSlots = ""
    private var solutions = mutableMapOf<LeverBlock, Array<Double>>()
    private var openedWater = -1L

    @OptIn(DelicateCoroutinesApi::class)
    fun scan() {
        if (DungeonUtils.currentRoomName != "Water Board") return
        GlobalScope.launch {
            prevInWaterRoom = inWaterRoom
            inWaterRoom = false

            val x = DungeonUtils.currentRoom?.room?.x ?: return@launch
            val z = DungeonUtils.currentRoom?.room?.z ?: return@launch

            for (direction in EnumFacing.HORIZONTALS) {
                val stairPos = BlockPos(x + direction.opposite.frontOffsetX * 4, 56, z + direction.opposite.frontOffsetZ * 4)
                if (mc.theWorld.getBlockState(stairPos).block == Blocks.stone_brick_stairs) {
                    roomFacing = direction
                    chestPos = stairPos.offset(direction, 11)

                    solve()
                    return@launch
                }
            }
        }
    }


    fun waterRender() {
        val sortedSolutions = mutableListOf<Double>().apply {
            solutions.forEach { (lever, times) ->
                times.drop(lever.i).filter { it != 0.0 }.forEach { time ->
                    add(time)
                }
            }
        }.sortedBy { it }
        for (solution in solutions) {
            // Draw order
            var orderText = ""

            solution.value.drop(solution.key.i).forEach {
                orderText = if (it == 0.0) orderText.plus("0")
                else orderText.plus("${if (orderText.isEmpty()) "" else ", "}${sortedSolutions.indexOf(it) + 1}")
            }
            if (showOrder)
                Renderer.drawStringInWorld(orderText, Vec3(solution.key.leverPos).addVector(.5, .5, .5), Color.WHITE, false, scale = .035f, depth = true)

            for (i in solution.key.i until solution.value.size) {
                val time = solution.value[i]
                val displayText = if (openedWater == -1L) {
                    if (time == 0.0) "§a§lCLICK ME!"
                    else "§e${time}s"
                } else {
                    val remainingTime = openedWater + time * 1000L - System.currentTimeMillis()
                    if (remainingTime > 0) "§e${remainingTime / 1000}s"
                    else "§a§lCLICK ME!"
                }

                Renderer.drawStringInWorld(displayText, Vec3(solution.key.leverPos).addVector(0.5, (i - solution.key.i) * 0.5 + 1.5, 0.5), Color.WHITE, false, depth = true, scale = 0.04f)
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

    private fun solve() {
        // Find the piston head block.
        val pistonHeadPos = chestPos!!.offset(roomFacing, 5).up(26)
        if (mc.theWorld.getBlockState(pistonHeadPos).block !== Blocks.piston_head) return

        // If the piston head block is found, then we are in the water room.
        inWaterRoom = true
        if (prevInWaterRoom) return
        // Find the blocks in the box around the piston head block.
        val blockList = BlockPos.getAllInBox(BlockPos(pistonHeadPos.x + 1, 78, pistonHeadPos.z + 1), BlockPos(pistonHeadPos.x - 1, 77, pistonHeadPos.z - 1))

        // Check for the required blocks in the box.
        var foundGold = false
        var foundClay = false
        var foundEmerald = false
        var foundQuartz = false
        var foundDiamond = false
        for (blockPos in blockList) {
            when (mc.theWorld.getBlockState(blockPos).block) {
                Blocks.gold_block -> foundGold = true
                Blocks.hardened_clay -> foundClay = true
                Blocks.emerald_block -> foundEmerald = true
                Blocks.quartz_block -> foundQuartz = true
                Blocks.diamond_block -> foundDiamond = true
            }
        }

        // If the required blocks are found, then set the variant and extendedSlots.
        variant = when {
            foundGold && foundClay -> 0
            foundEmerald && foundQuartz -> 1
            foundQuartz && foundDiamond -> 2
            foundGold && foundQuartz -> 3
            else -> -1
        }

        extendedSlots = ""
        WoolColor.entries.filter { it.isExtended }.forEach { extendedSlots += it.ordinal.toString() }

        // If the extendedSlots length is not 3, then retry.
        if (extendedSlots.length != 3) {
            extendedSlots = ""
            inWaterRoom = false
            prevInWaterRoom = false
            variant = -1
            return
        }

        // Print the variant and extendedSlots.
        modMessage("Variant: $variant:$extendedSlots:${roomFacing?.name}")

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

    fun reset() {
        chestPos = null
        roomFacing = null
        prevInWaterRoom = false
        inWaterRoom = false
        variant = -1
        extendedSlots = ""
        solutions.clear()
        openedWater = -1L
        LeverBlock.entries.forEach { it.i = 0 }
        scan()
    }

    enum class WoolColor {
        PURPLE, ORANGE, BLUE, GREEN, RED;

        val isExtended: Boolean
            get() =
                if (chestPos == null || roomFacing == null)
                    false
                else
                    mc.theWorld.getBlockState(chestPos?.offset(roomFacing!!.opposite, 3 + ordinal) ?: BlockPos(0,0,0)).block === Blocks.wool
    }

    enum class LeverBlock(var i: Int = 0) {
        QUARTZ, GOLD, COAL, DIAMOND, EMERALD, CLAY, WATER, NONE;

        val leverPos: BlockPos?
            get() {
                if (chestPos == null || roomFacing == null) return null
                return if (this == WATER) {
                    chestPos!!.offset(roomFacing!!.opposite, 17).up(4)
                } else {
                    val facing = roomFacing ?: return null
                    val shiftBy = ordinal % 3 * 5
                    val leverSide = if (ordinal < 3) facing.rotateY() else facing.rotateYCCW()
                    chestPos?.up(5)?.offset(leverSide.opposite, 6)?.offset(facing.opposite, 2 + shiftBy)?.offset(leverSide)
                }
            }
    }
}