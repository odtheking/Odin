package me.odinclient.features.impl.floor7.p3

import me.odinclient.utils.skyblock.PlayerUtils
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.NumberSetting
import me.odinmain.utils.clock.Clock
import me.odinmain.utils.render.RenderUtils.renderX
import me.odinmain.utils.render.RenderUtils.renderY
import me.odinmain.utils.render.RenderUtils.renderZ
import me.odinmain.utils.skyblock.*
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.skyblock.dungeon.M7Phases
import net.minecraft.util.*
import kotlin.math.sqrt

object Arrows : Module(
    name = "Arrows Triggerbot",
    description = "Trigger bot for 4th device in phase 3 of floor 7.",
    category = Category.FLOOR7
)  {
    private val triggerBotDelay: Long by NumberSetting("Delay", 250L, 50L, 1000L, 10L)
    private val triggerBotClock = Clock(triggerBotDelay)

    init {
        execute(10) {
            if (!triggerBotClock.hasTimePassed(triggerBotDelay) || mc.thePlayer?.heldItem?.isShortbow == false || DungeonUtils.getPhase() != M7Phases.P3) return@execute
            setBowTrajectoryHeading(0f)
            if (mc.thePlayer?.heldItem?.itemID == "TERMINATOR") {
                setBowTrajectoryHeading(-5f)
                setBowTrajectoryHeading(5f)
            }
        }
    }

    private fun setBowTrajectoryHeading(yawOffset: Float) {
        val yawRadians = ((mc.thePlayer.rotationYaw + yawOffset) / 180) * Math.PI.toFloat()
        val pitchRadians = (mc.thePlayer.rotationPitch / 180) * Math.PI.toFloat()

        var posX = mc.thePlayer.renderX
        var posY = mc.thePlayer.renderY + mc.thePlayer.eyeHeight
        var posZ = mc.thePlayer.renderZ
        posX -= (MathHelper.cos(mc.thePlayer.rotationYaw / 180.0f * Math.PI.toFloat()) * 0.16f).toDouble()
        posY -= 0.1
        posZ -= (MathHelper.sin(mc.thePlayer.rotationYaw / 180.0f * Math.PI.toFloat()) * 0.16f).toDouble()

        var motionX = (-MathHelper.sin(yawRadians) * MathHelper.cos(pitchRadians)).toDouble()
        var motionY = -MathHelper.sin(pitchRadians).toDouble()
        var motionZ = (MathHelper.cos(yawRadians) * MathHelper.cos(pitchRadians)).toDouble()

        val lengthOffset = sqrt(motionX * motionX + motionY * motionY + motionZ * motionZ)
        motionX = motionX / lengthOffset * 3
        motionY = motionY / lengthOffset * 3
        motionZ = motionZ / lengthOffset * 3

        calculateBowTrajectory(Vec3(motionX,motionY,motionZ),Vec3(posX,posY,posZ))
    }

    private fun calculateBowTrajectory(mV: Vec3, pV: Vec3) {
        var motionVec = mV
        var posVec = pV
        for (i in 0..20) {
            val vec = motionVec.add(posVec)
            val rayTrace = mc.theWorld.rayTraceBlocks(posVec, vec, false, true, false)
            if (rayTrace?.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                if (getBlockIdAt(rayTrace.blockPos) == 133) {
                    if (rayTrace.blockPos.x !in 64..68 || rayTrace.blockPos.y !in 126..130) return // not on device
                    PlayerUtils.rightClick()
                    triggerBotClock.update()
                }
                break
            }
            posVec = posVec.add(motionVec)
            motionVec = Vec3(motionVec.xCoord * 0.99, motionVec.yCoord * 0.99 - 0.05, motionVec.zCoord * 0.99)
        }
    }
}