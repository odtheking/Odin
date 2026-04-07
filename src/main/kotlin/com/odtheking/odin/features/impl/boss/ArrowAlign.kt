package com.odtheking.odin.features.impl.boss

import com.odtheking.odin.clickgui.settings.Setting.Companion.withDependency
import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.clickgui.settings.impl.KeybindSetting
import com.odtheking.odin.clickgui.settings.impl.KeybindSetting.Companion.isDown
import com.odtheking.odin.events.EntityInteractEvent
import com.odtheking.odin.events.RenderEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.addVec
import com.odtheking.odin.utils.component1
import com.odtheking.odin.utils.component2
import com.odtheking.odin.utils.component3
import com.odtheking.odin.utils.handlers.TickTask
import com.odtheking.odin.utils.render.drawText
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import com.odtheking.odin.utils.skyblock.dungeon.M7Phases
import net.minecraft.core.BlockPos
import net.minecraft.world.entity.decoration.ItemFrame
import net.minecraft.world.item.Items
import org.lwjgl.glfw.GLFW

object ArrowAlign : Module(
    name = "Arrow Align",
    description = "Shows the solution for the Arrow Align device."
) {
    private val blockWrong by BooleanSetting("Block Wrong Clicks", false, desc = "Blocks wrong clicks, shift will override this.")
    private val preventKey by KeybindSetting("Prevent Blocking", GLFW.GLFW_KEY_LEFT_SHIFT, desc = "While holding this key, wrong clicks won't be blocked, even if you aren't sneaking.").withDependency { blockWrong }
    private val invertKey by BooleanSetting("Invert Key", false, desc = "Inverts the behavior of the prevent key. Wrong clicks will be blocked while holding the prevent key, and allowed otherwise.").withDependency { blockWrong }

    private val recentClickTimestamps = mutableMapOf<Int, Long>()
    private val clicksRemaining = mutableMapOf<Int, Int>()
    private var currentFrameRotations: List<Int>? = null
    private var targetSolution: List<Int>? = null
    private val frameGridCorner = BlockPos(-2, 120, 75)
    private val centerBlock = BlockPos(0, 120, 77)

    init {
        TickTask(1) {
            if (!enabled || DungeonUtils.getF7Phase() != M7Phases.P3) return@TickTask
            clicksRemaining.clear()
            if ((mc.player?.blockPosition()?.distSqr(centerBlock) ?: return@TickTask) > 200) {
                currentFrameRotations = null
                targetSolution = null
                return@TickTask
            }

            currentFrameRotations = getFrames()

            possibleSolutions.forEach { arr ->
                for (i in arr.indices) {
                    if ((arr[i] == -1 || currentFrameRotations?.get(i) == -1) && arr[i] != currentFrameRotations?.get(i)) return@forEach
                }

                targetSolution = arr

                for (i in arr.indices) {
                    clicksRemaining[i] = calculateClicksNeeded(currentFrameRotations?.get(i) ?: return@forEach, arr[i]).takeIf { it != 0 } ?: continue
                }
            }
        }

        on<EntityInteractEvent> {
            if (DungeonUtils.getF7Phase() != M7Phases.P3) return@on
            if (entity !is ItemFrame || entity.item.item != Items.ARROW) return@on
            val (x, y, z) = entity.blockPosition()

            val frameIndex = ((y - frameGridCorner.y) + (z - frameGridCorner.z) * 5)
            if (x != frameGridCorner.x || currentFrameRotations?.get(frameIndex) == -1 || frameIndex !in 0..24) return@on

            val shouldBlock = blockWrong && !(preventKey.isDown() xor invertKey)

            if (!clicksRemaining.containsKey(frameIndex) && shouldBlock) {
                cancel()
                return@on
            }

            recentClickTimestamps[frameIndex] = System.currentTimeMillis()
            currentFrameRotations = currentFrameRotations?.toMutableList()?.apply { this[frameIndex] = (this[frameIndex] + 1) % 8 }

            if (calculateClicksNeeded(currentFrameRotations?.get(frameIndex) ?: return@on, targetSolution?.get(frameIndex) ?: return@on) == 0) clicksRemaining.remove(frameIndex)
        }

        on<RenderEvent.Extract> {
            if (clicksRemaining.isEmpty() || DungeonUtils.getF7Phase() != M7Phases.P3) return@on
            clicksRemaining.forEach { (index, clickNeeded) ->
                val colorCode = when {
                    clickNeeded == 0 -> return@forEach
                    clickNeeded < 3 -> 'a'
                    clickNeeded < 5 -> '6'
                    else -> 'c'
                }
                drawText(
                    "§$colorCode$clickNeeded",
                    getFramePositionFromIndex(index).center.addVec(y = 0.1, x = -0.3),
                    1f, false
                )
            }
        }
    }

    private fun getFrames(): List<Int> {
        val itemFrames = mc.level?.entitiesForRendering()?.mapNotNull {
            if (it is ItemFrame && it.item.item == Items.ARROW) it else null
        }?.takeIf { it.isNotEmpty() } ?: return List(25) { -1 }

        return (0..24).map { index ->
            if (recentClickTimestamps[index]?.let { System.currentTimeMillis() - it < 1000 } == true && currentFrameRotations != null)
                currentFrameRotations?.get(index) ?: -1
            else
                itemFrames.find { it.blockPosition() == getFramePositionFromIndex(index) }?.rotation ?: -1
        }
    }

    private fun getFramePositionFromIndex(index: Int): BlockPos =
        frameGridCorner.offset(0, index % 5, index / 5)

    private fun calculateClicksNeeded(currentRotation: Int, targetRotation: Int): Int =
        (8 - currentRotation + targetRotation) % 8

    private val possibleSolutions = listOf(
        listOf(7, 7, -1, -1, -1, 1, -1, -1, -1, -1, 1, 3, 3, 3, 3, -1, -1, -1, -1, 1, -1, -1, -1, 7, 1),
        listOf(-1, -1, 7, 7, 5, -1, 7, 1, -1, 5, -1, -1, -1, -1, -1, -1, 7, 5, -1, 1, -1, -1, 7, 7, 1),
        listOf(7, 7, -1, -1, -1, 1, -1, -1, -1, -1, 1, 3, -1, 7, 5, -1, -1, -1, -1, 5, -1, -1, -1, 3, 3),
        listOf(5, 3, 3, 3, -1, 5, -1, -1, -1, -1, 7, 7, -1, -1, -1, 1, -1, -1, -1, -1, 1, 3, 3, 3, -1),
        listOf(5, 3, 3, 3, 3, 5, -1, -1, -1, 1, 7, 7, -1, -1, 1, -1, -1, -1, -1, 1, -1, 7, 7, 7, 1),
        listOf(7, 7, 7, 7, -1, 1, -1, -1, -1, -1, 1, 3, 3, 3, 3, -1, -1, -1, -1, 1, -1, 7, 7, 7, 1),
        listOf(-1, -1, -1, -1, -1, 1, -1, 1, -1, 1, 1, -1, 1, -1, 1, 1, -1, 1, -1, 1, -1, -1, -1, -1, -1),
        listOf(-1, -1, -1, -1, -1, 1, 3, 3, 3, 3, -1, -1, -1, -1, 1, 7, 7, 7, 7, 1, -1, -1, -1, -1, -1),
        listOf(-1, -1, -1, -1, -1, -1, 1, -1, 1, -1, 7, 1, 7, 1, 3, 1, -1, 1, -1, 1, -1, -1, -1, -1, -1)
    )
}