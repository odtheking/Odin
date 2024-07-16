package me.odinclient.features.impl.floor7.p3

import me.odinclient.utils.skyblock.PlayerUtils
import me.odinmain.events.impl.ClickEvent
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.NumberSetting
import me.odinmain.utils.*
import me.odinmain.utils.clock.Clock
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.Renderer
import net.minecraft.entity.item.EntityItemFrame
import net.minecraft.init.Items
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object ArrowAlign : Module(
    name = "Arrow Align",
    description = "Different features for the arrow alignment device.",
    category = Category.FLOOR7
) {
    private val blockWrong: Boolean by BooleanSetting("Block Wrong Clicks", false, description = "Blocks wrong clicks, shift will override this")
    private val triggerBot: Boolean by BooleanSetting("Trigger Bot")
    private val sneakToDisableTriggerbot: Boolean by BooleanSetting("Sneak to disable", false, description = "Disables triggerbot when you are sneaking").withDependency { triggerBot }
    private val delay: Long by NumberSetting<Long>("Delay", 200, 70, 500).withDependency { triggerBot }

    private val triggerBotClock = Clock(delay)

    private val frameGridCorner = Vec3(-2.0, 120.0, 75.0)

    private val recentClickTimestamps = mutableMapOf<Int, Long>()
    private val clicksRemaining = mutableMapOf<Int, Int>()
    private var currentFrameRotations: List<Int>? = null
    private var targetSolution: List<Int>? = null

    init {
        execute(100) {
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
        val targetFrame = mc.objectMouseOver?.entityHit as? EntityItemFrame ?: return

        val targetFramePosition = targetFrame.positionVector.flooredVec()

        val frameIndex = ((targetFramePosition.yCoord - frameGridCorner.yCoord) + (targetFramePosition.zCoord - frameGridCorner.zCoord) * 5).toInt()
        if (targetFramePosition.xCoord != frameGridCorner.xCoord || currentFrameRotations?.get(frameIndex) == -1 || frameIndex !in 0..24) return

        if (!clicksRemaining.containsKey(frameIndex) && mc.thePlayer.isSneaking) {
            if (blockWrong) event.isCanceled = true
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
        if (clicksRemaining.isEmpty()) return
        triggerBot()
        clicksRemaining.forEach { (index, clickNeeded) ->
            val framePosition = getFramePositionFromIndex(index)
            val color = when {
                clickNeeded == 0 -> return@forEach
                clickNeeded < 3 -> Color(85, 255, 85)
                clickNeeded < 5 -> Color(255, 170, 0)
                else -> Color(170, 0, 0)
            }
            Renderer.drawStringInWorld(clickNeeded.toString(), framePosition.addVec(y = 0.6, z = 0.5), color)
        }
    }

    private fun getFrames(): List<Int> {
        val itemFrames = mc.theWorld.loadedEntityList
            .filterIsInstance<EntityItemFrame>()
            .filter { it.displayedItem?.item == Items.arrow }
        if (itemFrames.isEmpty()) return List(25) { -1 }

        val positionToRotationMap = itemFrames.associate { it.positionVector.flooredVec().toString() to it.rotation }

        return (0..24).map { index ->
            if (recentClickTimestamps[index]?.let { System.currentTimeMillis() - it < 1000 } == true && currentFrameRotations != null)
                currentFrameRotations?.get(index) ?: -1
            else
                positionToRotationMap[getFramePositionFromIndex(index).toString()] ?: -1
        }
    }

    private fun getFramePositionFromIndex(index: Int): Vec3 {
        return frameGridCorner.addVec(0, index % 5, index / 5)
    }

    private fun calculateClicksNeeded(currentRotation: Int, targetRotation: Int): Int {
        return (8 - currentRotation + targetRotation) % 8
    }

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

    private fun triggerBot() {
        if (!triggerBotClock.hasTimePassed(delay) || (sneakToDisableTriggerbot && mc.thePlayer.isSneaking)) return
        val targetFrame = mc.objectMouseOver?.entityHit as? EntityItemFrame ?: return

        val targetFramePosition = targetFrame.positionVector.flooredVec()

        val frameIndex = ((targetFramePosition.yCoord - frameGridCorner.yCoord) + (targetFramePosition.zCoord - frameGridCorner.zCoord) * 5).toInt()
        if (targetFramePosition.xCoord != frameGridCorner.xCoord || currentFrameRotations?.get(frameIndex) == -1 || frameIndex !in 0..24) return
        clicksRemaining[frameIndex]?.let {
            PlayerUtils.rightClick()
            triggerBotClock.update()
        }
    }
}