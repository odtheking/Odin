package com.odtheking.odin.features.impl.floor7

import com.odtheking.odin.clickgui.settings.Setting.Companion.withDependency
import com.odtheking.odin.clickgui.settings.impl.ActionSetting
import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.clickgui.settings.impl.ColorSetting
import com.odtheking.odin.events.BlockUpdateEvent
import com.odtheking.odin.events.ChatPacketEvent
import com.odtheking.odin.events.RenderEvent
import com.odtheking.odin.events.WorldEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.events.core.onReceive
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.Color.Companion.withAlpha
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.alert
import com.odtheking.odin.utils.modMessage
import com.odtheking.odin.utils.render.drawFilledBox
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import com.odtheking.odin.utils.skyblock.dungeon.M7Phases
import net.minecraft.core.BlockPos
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import kotlin.math.abs

object ArrowsDevice : Module(
    name = "Arrows Device",
    description = "Shows a solution for the Sharp Shooter puzzle in floor 7."
) {
    private val markedPositionColor by ColorSetting("Marked Position", Colors.MINECRAFT_AQUA.withAlpha(0.5f), true, desc = "Color of the marked position.")
    private val targetPositionColor by ColorSetting("Target Position", Colors.MINECRAFT_LIGHT_PURPLE.withAlpha(0.5f), true, desc = "Color of the target position.")
    private val depthCheck by BooleanSetting("Depth check", true, desc = "Marked positions show through walls.")
    private val alertOnDeviceComplete by BooleanSetting("Device complete alert", true, desc = "Send an alert when device is complete.")
    private val showAimPositions by BooleanSetting("Show Aim Positions", false, desc = "Shows optimal aim positions for hitting marked blocks.")
    private val firstAimPositionColor by ColorSetting("First Aim Position", Colors.MINECRAFT_GREEN.withAlpha(0.5f), true, desc = "Color of the first aim position.").withDependency { showAimPositions }
    private val secondAimPositionColor by ColorSetting("Second Aim Position", Colors.MINECRAFT_GOLD.withAlpha(0.5f), true, desc = "Color of the second aim position.").withDependency { showAimPositions }
    private val thirdAimPositionColor by ColorSetting("Third Aim Position", Colors.MINECRAFT_RED.withAlpha(0.5f), true, desc = "Color of the third aim position.").withDependency { showAimPositions }
    private val reset by ActionSetting("Reset", desc = "Resets the solver.") {
        markedPositions.clear()
        targetPosition = null
        optimalAimPositions = emptyList()
    }

    private val deviceCompleteRegex = Regex("^(.{1,16}) completed a device! \\((\\d)/(\\d)\\)$")
    private val roomBoundingBox = AABB(20.0, 100.0, 30.0, 89.0, 151.0, 51.0)
    private val markedPositions = mutableSetOf<BlockPos>()
    private var targetPosition: BlockPos? = null
    private var isDeviceComplete = false

    private data class AimPosition(val position: Vec3, val coveredBlocks: Set<BlockPos>, val distance: Double)

    private var optimalAimPositions: List<AimPosition> = emptyList()

    private val adjacentPairs by lazy {
        devicePositions.flatMapIndexed { i, block1 ->
            devicePositions.drop(i + 1).mapNotNull { block2 ->
                if (abs(block1.x - block2.x) == 2 && block1.y == block2.y && block1.z == block2.z) block1 to block2 else null
            }
        }
    }

    init {
        on<BlockUpdateEvent> {
            if (DungeonUtils.getF7Phase() != M7Phases.P3 || !devicePositions.contains(pos)) return@on

            if (old.block == Blocks.EMERALD_BLOCK && updated.block == Blocks.BLUE_TERRACOTTA) {
                markedPositions.add(pos.immutable())
                if (targetPosition == pos) targetPosition = null
                if (showAimPositions) optimalAimPositions = calculateOptimalAimPositions(pos)
            } else if (old.block == Blocks.BLUE_TERRACOTTA && updated.block == Blocks.EMERALD_BLOCK) {
                markedPositions.remove(pos)
                targetPosition = pos.immutable()
                if (showAimPositions) optimalAimPositions = calculateOptimalAimPositions(pos)
            }
        }

        on<RenderEvent.Extract> {
            if (DungeonUtils.getF7Phase() != M7Phases.P3) return@on

            markedPositions.forEach { position ->
                drawFilledBox(AABB(position), markedPositionColor, depth = depthCheck)
            }

            targetPosition?.let { position ->
                drawFilledBox(AABB(position), targetPositionColor, depth = depthCheck)
            }

            if (showAimPositions) {
                val colors = listOf(firstAimPositionColor, secondAimPositionColor, thirdAimPositionColor)
                optimalAimPositions.take(3).forEachIndexed { index, aimPos ->
                    drawFilledBox(AABB.unitCubeFromLowerCorner(aimPos.position.add(-0.5, -0.5, -0.1)), colors[index], true)
                }
            }
        }

        on<WorldEvent.Load> {
            markedPositions.clear()
            targetPosition = null
            isDeviceComplete = false
            optimalAimPositions = emptyList()
        }

        on<ChatPacketEvent> {
            if (DungeonUtils.getF7Phase() != M7Phases.P3 || !isPlayerInRoom || isDeviceComplete) return@on
            if (deviceCompleteRegex.find(value)?.groupValues?.get(1) == mc.player?.name?.string) onComplete("Chat")
        }

        onReceive<ClientboundSetEntityDataPacket> {
            if (DungeonUtils.getF7Phase() != M7Phases.P3 || !isPlayerInRoom || isDeviceComplete) return@onReceive
            if (mc.level?.getEntity(id)?.name?.string == "Active") onComplete("Entity")
        }
    }

    private fun onComplete(method: String) {
        isDeviceComplete = true

        if (alertOnDeviceComplete) {
            modMessage("§aSharp shooter device complete §7($method)")
            alert("§aDevice Complete")
        }
        reset()
    }

    private val isPlayerInRoom: Boolean
        get() = mc.player?.let { roomBoundingBox.contains(it.position()) } == true

    private fun calculateOptimalAimPositions(target: BlockPos): List<AimPosition> {
        val unmarkedBlocks = devicePositions.filterNot { it in markedPositions }

        fun createAimPosition(block1: BlockPos, block2: BlockPos): AimPosition? =
            listOf(block1, block2).filter { it in unmarkedBlocks }.takeIf { it.isNotEmpty() }?.let {
                AimPosition(block1.midpoint(block2), it.toSet(), 0.0)
            }

        val greenAim = adjacentPairs
            .filter { (block1, block2) -> target in listOf(block1, block2) }
            .mapNotNull { (block1, block2) -> createAimPosition(block1, block2) }
            .maxByOrNull { it.coveredBlocks.size } ?: return emptyList()

        val remainingAimPositions = adjacentPairs
            .filterNot { (block1, block2) -> target in listOf(block1, block2) }
            .mapNotNull { (block1, block2) -> createAimPosition(block1, block2) }

        return findBestCombination(greenAim, remainingAimPositions)
    }

    private fun findBestCombination(greenAim: AimPosition, aimPositions: List<AimPosition>): List<AimPosition> {
        val result = mutableListOf(greenAim)
        val covered = greenAim.coveredBlocks.toMutableSet()

        repeat(2) {
            val best = aimPositions.filterNot { it in result }
                .maxWithOrNull(compareBy(
                    { it.coveredBlocks.count { block -> block !in covered } }, // New unmarked blocks
                    { it.coveredBlocks.size }, // Total blocks covered
                    { -(result.last().position.distanceTo(it.position)) } // Closer to last position
                )) ?: return@repeat

            result.add(best)
            covered.addAll(best.coveredBlocks)
        }

        return result.mapIndexed { index, aimPos ->
            AimPosition(aimPos.position, aimPos.coveredBlocks, if (index == 0) 0.0 else aimPos.position.distanceTo(result[index - 1].position))
        }
    }

    private fun BlockPos.midpoint(other: BlockPos): Vec3 =
        Vec3((x + other.x) / 2.0 + 0.5, y.toDouble() + 0.5, z.toDouble() + 0.5)

    private val devicePositions = listOf(
        BlockPos(68, 130, 50), BlockPos(66, 130, 50), BlockPos(64, 130, 50),
        BlockPos(68, 128, 50), BlockPos(66, 128, 50), BlockPos(64, 128, 50),
        BlockPos(68, 126, 50), BlockPos(66, 126, 50), BlockPos(64, 126, 50)
    )
}