package me.odinmain.features.impl.floor7.p3

import me.odinmain.events.impl.ClickEvent
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.utils.*
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.Renderer
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.skyblock.dungeon.M7Phases
import net.minecraft.entity.item.EntityItemFrame
import net.minecraft.init.Items
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set

object ArrowAlign : Module(
    name = "Arrow Align",
    description = "Shows a solution for the Arrow Align device.",
    category = Category.FLOOR7
) {
    private val blockWrong by BooleanSetting("Block Wrong Clicks", false, description = "Blocks wrong clicks, shift will override this.")

    private val frameGridCorner = Vec3(-2.0, 120.0, 75.0)
    private val recentClickTimestamps = mutableMapOf<Int, Long>()
    val clicksRemaining = mutableMapOf<Int, Int>()
    var currentFrameRotations: List<Int>? = null
    private var targetSolution: List<Int>? = null

    init {
        execute(100) {
            if (DungeonUtils.getF7Phase() != M7Phases.P3) return@execute
            clicksRemaining.clear()
            if ((mc.thePlayer?.distanceSquaredTo(Vec3(0.0, 120.0, 77.0)) ?: return@execute) > 200) {
                currentFrameRotations = null
                targetSolution = null
                return@execute
            }

            currentFrameRotations = getFrames()

            possibleSolutions.forEach { arr ->
                for (i in arr.indices) {
                    if ((arr[i] == -1 || currentFrameRotations?.get(i) == -1) && arr[i] != currentFrameRotations?.get(i)) return@forEach
                }

                for (i in arr.indices) {
                    clicksRemaining[i] = calculateClicksNeeded(currentFrameRotations?.get(i) ?: return@forEach, arr[i]).takeIf { it != 0 } ?: continue
                }
            }
        }
    }

    @SubscribeEvent
    fun onRightClick(event: ClickEvent.RightClickEvent) {
        if (DungeonUtils.getF7Phase() != M7Phases.P3) return
        val targetFrame = mc.objectMouseOver?.entityHit as? EntityItemFrame ?: return
        val targetFramePosition = targetFrame.positionVector.flooredVec()

        val frameIndex = ((targetFramePosition.yCoord - frameGridCorner.yCoord) + (targetFramePosition.zCoord - frameGridCorner.zCoord) * 5).toInt()
        if (targetFramePosition.xCoord != frameGridCorner.xCoord || currentFrameRotations?.get(frameIndex) == -1 || frameIndex !in 0..24) return

        if (!clicksRemaining.containsKey(frameIndex) && !mc.thePlayer.isSneaking && blockWrong) {
            event.isCanceled = true
            return
        }

        recentClickTimestamps[frameIndex] = System.currentTimeMillis()
        currentFrameRotations = currentFrameRotations?.toMutableList()?.apply { this[frameIndex] = (this[frameIndex] + 1) % 8 }

        currentFrameRotations?.let {
            val target = targetSolution ?: return
            val remainingClicks = calculateClicksNeeded(it[frameIndex], target[frameIndex])
            if (remainingClicks == 0) clicksRemaining.remove(frameIndex)
        }
    }

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        if (clicksRemaining.isEmpty() || DungeonUtils.getF7Phase() != M7Phases.P3) return
        clicksRemaining.forEach { (index, clickNeeded) ->
            val color = when {
                clickNeeded == 0 -> return@forEach
                clickNeeded < 3 -> Color(85, 255, 85)
                clickNeeded < 5 -> Color(255, 170, 0)
                else -> Color(170, 0, 0)
            }
            Renderer.drawStringInWorld(clickNeeded.toString(), getFramePositionFromIndex(index).addVec(y = 0.6, z = 0.5), color)
        }
    }

    private fun getFrames(): List<Int> {
        val itemFrames = mc.theWorld?.loadedEntityList?.mapNotNull {
            if (it is EntityItemFrame && it.displayedItem?.item == Items.arrow) it else null }?.takeIf { it.isNotEmpty() } ?: return List(25) { -1 }

        return (0..24).map { index ->
            if (recentClickTimestamps[index]?.let { System.currentTimeMillis() - it < 1000 } == true && currentFrameRotations != null)
                currentFrameRotations?.get(index) ?: -1
            else
                itemFrames.associate { it.positionVector.flooredVec().toString() to it.rotation }[getFramePositionFromIndex(index).toString()] ?: -1
        }
    }

    private fun getFramePositionFromIndex(index: Int): Vec3 =
        frameGridCorner.addVec(0, index % 5, index / 5)

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