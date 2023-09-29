package me.odinclient.features.impl.floor7.p3

import me.odinclient.features.Category
import me.odinclient.features.Module
import me.odinclient.features.impl.render.ArrowTrajectory
import me.odinclient.features.settings.impl.BooleanSetting
import me.odinclient.features.settings.impl.NumberSetting
import me.odinclient.utils.clock.Clock
import me.odinclient.utils.render.world.RenderUtils.renderX
import me.odinclient.utils.render.world.RenderUtils.renderY
import me.odinclient.utils.render.world.RenderUtils.renderZ
import me.odinclient.utils.skyblock.ChatUtils.modMessage
import me.odinclient.utils.skyblock.ItemUtils.isShortbow
import me.odinclient.utils.skyblock.ItemUtils.itemID
import me.odinclient.utils.skyblock.PlayerUtils
import me.odinclient.utils.skyblock.WorldUtils
import me.odinclient.utils.skyblock.dungeon.DungeonUtils
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.projectile.EntityArrow
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.MovingObjectPosition
import net.minecraft.util.Vec3
import net.minecraftforge.event.entity.player.PlayerInteractEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import javax.vecmath.Vector2d
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object Arrows : Module(
    name = "Arrows Triggerbot",
    description = "Trigger bot for 4th device.",
    category = Category.FLOOR7,
    tag = TagType.NEW
)  {
    private val triggerBotDelay: Long by NumberSetting("Delay", 250L, 50L, 1000L, 10L)
    private val triggerBotClock = Clock(triggerBotDelay)

    init {
        execute(10) {
            if (!triggerBotClock.hasTimePassed(triggerBotDelay) || mc.thePlayer?.heldItem?.isShortbow == false || DungeonUtils.getPhase() != 3) return@execute
            setTrajectoryHeading(0f, -0.1f)
            if (mc.thePlayer?.heldItem?.itemID != "TERMINATOR") {
                setTrajectoryHeading(-5f, 0f)
                setTrajectoryHeading(5f, 0f)
            }
        }
    }

    private fun setTrajectoryHeading(yawOffset: Float, yOffset: Float) {
        val yawRadians = ((mc.thePlayer.rotationYaw + yawOffset) / 180) * Math.PI
        val pitchRadians = (mc.thePlayer.rotationPitch / 180) * Math.PI
        val motionX = -sin(yawRadians) * cos(pitchRadians)
        val motionY = -sin(pitchRadians)
        val motionZ = cos(yawRadians) * cos(pitchRadians)
        val lengthOffset = sqrt(motionX * motionX + motionY * motionY + motionZ * motionZ)

        calculateTrajectory(
            Vec3(
                motionX / lengthOffset * 3,
                motionY / lengthOffset * 3,
                motionZ / lengthOffset * 3
            ),
            Vec3(mc.thePlayer.renderX, mc.thePlayer.renderY + mc.thePlayer.eyeHeight + yOffset, mc.thePlayer.renderZ)
        )
    }

    private fun calculateTrajectory(mV: Vec3, pV: Vec3) {
        var motionVec = mV
        var posVec = pV
        for (i in 0..60) {
            val vec = motionVec.add(posVec)
            val rayTrace = mc.theWorld.rayTraceBlocks(posVec, vec, false, true, false)
            if (rayTrace?.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                if (WorldUtils.getBlockIdAt(rayTrace.blockPos) == 133) {
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