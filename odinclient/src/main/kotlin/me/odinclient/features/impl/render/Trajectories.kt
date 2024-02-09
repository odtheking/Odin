package me.odinclient.features.impl.render

import me.odinmain.events.impl.RenderEntityModelEvent
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.ColorSetting
import me.odinmain.features.settings.impl.NumberSetting
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.world.OutlineUtils
import me.odinmain.utils.render.world.RenderUtils
import me.odinmain.utils.render.world.RenderUtils.renderX
import me.odinmain.utils.render.world.RenderUtils.renderY
import me.odinmain.utils.render.world.RenderUtils.renderZ
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.skyblock.isShortbow
import me.odinmain.utils.skyblock.itemID
import me.odinmain.utils.skyblock.modMessage
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.monster.EntityBlaze
import net.minecraft.entity.monster.EntityMagmaCube
import net.minecraft.entity.projectile.EntityArrow
import net.minecraft.item.ItemBow
import net.minecraft.item.ItemEnderPearl
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.MathHelper
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import javax.vecmath.Vector2d
import kotlin.math.sqrt

object Trajectories : Module(
    "Trajectories",
    description = "Displays the trajectory of certain items",
    category = Category.RENDER,
    tag = TagType.NEW
) {
    private val bows: Boolean by BooleanSetting("Bows", false, description = "Render trajectories of bow arrows")
    private val pearls: Boolean by BooleanSetting("Pearls", false, description = "Render trajectories of ender pearls")

    private val range: Float by NumberSetting("Solver Range", 30f, 1f, 60f, 1f, description = "Performance impact scales with this")
    private val thickness: Float by NumberSetting("Line Width", 2f, 1.0, 5.0, 0.5)
    private val boxSize: Float by NumberSetting("Box Size", 0.5f, 0.5f, 3.0f, 0.1f)
    private val color: Color by ColorSetting("Color", Color(170, 170, 0), true)

    private var boxRenderQueue: MutableList<Pair<Vec3, Vector2d>> = mutableListOf()
    private var entityRenderQueue = mutableListOf<Entity>()
    private var lineRenderQueue = arrayListOf<Vec3>()

    private var pearlImpactPos: Pair<Vec3,Vector2d>? = null

    @SubscribeEvent
    fun onRenderWorldLast(event: RenderWorldLastEvent) {
        entityRenderQueue.clear()
        lineRenderQueue.clear()
        if (bows && mc.thePlayer?.heldItem?.item is ItemBow) {
            if (mc.thePlayer?.heldItem?.isShortbow == true) {
                if (mc.thePlayer?.heldItem?.itemID == "TERMINATOR") {
                    this.setBowTrajectoryHeading(-5f, false)
                    this.setBowTrajectoryHeading(0f, false)
                    this.setBowTrajectoryHeading(5f, false)
                }
                else {
                    this.setBowTrajectoryHeading(0f, false)
                }
            } else {
                if (mc.thePlayer?.itemInUseDuration == 0) return
                this.setBowTrajectoryHeading(0f, true)
            }
            this.drawBowCollisionBoxes()
        }
        if (pearls) {
            pearlImpactPos = null
            val itemStack = mc.thePlayer?.heldItem
            if (itemStack?.item is ItemEnderPearl && !itemStack.displayName.contains("leap", ignoreCase = true)) {
                this.setPearlTrajectoryHeading()
                this.drawPearlCollisionBox()
            }
        }
    }

    private fun setPearlTrajectoryHeading() {
        var motionX =
            (-MathHelper.sin(mc.thePlayer.rotationYaw / 180.0f * Math.PI.toFloat()) * MathHelper.cos(mc.thePlayer.rotationPitch / 180.0f * Math.PI.toFloat()) * 0.4)
        var motionZ =
            (MathHelper.cos(mc.thePlayer.rotationYaw / 180.0f * Math.PI.toFloat()) * MathHelper.cos(mc.thePlayer.rotationPitch / 180.0f * Math.PI.toFloat()) * 0.4)
        var motionY =
            (-MathHelper.sin(mc.thePlayer.rotationPitch / 180.0f * Math.PI.toFloat()) * 0.4)
        var posX = mc.thePlayer.renderX
        var posY = mc.thePlayer.renderY + mc.thePlayer.eyeHeight
        var posZ = mc.thePlayer.renderZ
        posX -= (MathHelper.cos(mc.thePlayer.rotationYaw / 180.0f * Math.PI.toFloat()) * 0.16f).toDouble()
        posY -= 0.1f
        posZ -= (MathHelper.sin(mc.thePlayer.rotationYaw / 180.0f * Math.PI.toFloat()) * 0.16f).toDouble()
        val f = MathHelper.sqrt_double(motionX * motionX + motionY * motionY + motionZ * motionZ)
        motionX /= f
        motionY /= f
        motionZ /= f
        motionX *= 1.5f
        motionY *= 1.5f
        motionZ *= 1.5f

        calculatePearlTrajectory(Vec3(motionX, motionY, motionZ), Vec3(posX, posY, posZ))
    }

    private fun calculatePearlTrajectory(mV: Vec3,pV: Vec3) {
        var hitResult = false
        var motionVec = mV
        var posVec = pV
        for (i in 0..range.toInt()) {
            if (hitResult) break
            val vec = motionVec.add(posVec)
            val rayTrace = mc.theWorld.rayTraceBlocks(posVec, vec, false, true, false)
            if (rayTrace != null) {
                pearlImpactPos =
                    Pair(
                        rayTrace.hitVec.addVector(-0.15 * boxSize, 0.0, -0.15 * boxSize),
                        Vector2d(0.3 * boxSize, 0.3 * boxSize)
                    )
                hitResult = true
            }
            posVec = posVec.add(motionVec)
            motionVec = Vec3(motionVec.xCoord * 0.99, motionVec.yCoord * 0.99 - 0.03, motionVec.zCoord * 0.99)
        }
    }

    private fun setBowTrajectoryHeading(yawOffset: Float, bowCharge: Boolean) {
        var charge = 2f
        if (bowCharge) {
            charge = (72000 - mc.thePlayer.itemInUseCount) / 20f
            if (charge > 1.0f) charge = 1.0f
            charge *= 2
        }

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
        motionX = motionX / lengthOffset * charge * 1.5f
        motionY = motionY / lengthOffset * charge * 1.5f
        motionZ = motionZ / lengthOffset * charge * 1.5f

        calculateBowTrajectory(Vec3(motionX, motionY, motionZ), Vec3(posX, posY, posZ))
    }

    private fun calculateBowTrajectory(mV: Vec3,pV: Vec3) {
        var hitResult = false
        var motionVec = mV
        var posVec = pV
        for (i in 0..range.toInt()) {
            if (hitResult) break
            val vec = motionVec.add(posVec)
            val rayTrace = mc.theWorld.rayTraceBlocks(posVec, vec, false, true, false)
            val aabb = AxisAlignedBB(0.0, 0.0, 0.0, 0.0, 0.0, 0.0)
                .offset(posVec.xCoord, posVec.yCoord, posVec.zCoord)
                .addCoord(motionVec.xCoord, motionVec.yCoord, motionVec.zCoord)
                .expand(0.01, 0.01, 0.01)
            val entityHit = mc.theWorld.getEntitiesWithinAABBExcludingEntity(mc.thePlayer, aabb).filter { it !is EntityArrow && it !is EntityArmorStand}
            if (entityHit.isNotEmpty()) {
                hitResult = true
                entityRenderQueue.addAll(entityHit)
            } else if (rayTrace != null) {
                boxRenderQueue.add(
                    Pair(
                        rayTrace.hitVec.addVector(-0.15 * boxSize, 0.0, -0.15 * boxSize),
                        Vector2d(0.3 * boxSize, 0.3 * boxSize)
                    )
                )
                hitResult = true
            }
            posVec = posVec.add(motionVec)
            motionVec = Vec3(motionVec.xCoord * 0.99, motionVec.yCoord * 0.99 - 0.05, motionVec.zCoord * 0.99)
        }
    }

    private fun drawPearlCollisionBox() {
        if (pearlImpactPos == null) return
        RenderUtils.drawBoxOutline(
            pearlImpactPos!!.first.xCoord, pearlImpactPos!!.second.x,
            pearlImpactPos!!.first.yCoord, pearlImpactPos!!.second.y,
            pearlImpactPos!!.first.zCoord, pearlImpactPos!!.second.x,
            color,
            thickness / 3,
            phase = true
        )
        pearlImpactPos = null
    }

    private fun drawBowCollisionBoxes() {
        if (boxRenderQueue.size == 0) return
        for (b in boxRenderQueue) {
            if (
                hypot(
                    mc.thePlayer.renderX - b.first.xCoord,
                    mc.thePlayer.renderY + mc.thePlayer.eyeHeight - b.first.yCoord,
                    mc.thePlayer.renderZ - b.first.zCoord
                ) < 2
            ) {
                boxRenderQueue.clear()
                return
            }
            RenderUtils.drawBoxOutline(
                b.first.xCoord, b.second.x,
                b.first.yCoord, b.second.y,
                b.first.zCoord, b.second.x,
                color,
                thickness / 3,
                phase = true
            )
        }
        boxRenderQueue.clear()
    }

    @SubscribeEvent
    fun onRenderModel(event: RenderEntityModelEvent) {
        if (event.entity !in entityRenderQueue) return
        if (!mc.thePlayer.canEntityBeSeen(event.entity)) return
        if (!bows || mc.thePlayer?.heldItem?.item !is ItemBow) return
        if(event.entity is EntityBlaze && DungeonUtils.inDungeons) return
        if (event.entity is EntityMagmaCube) modMessage((event.entity as EntityMagmaCube).slimeSize)
        OutlineUtils.outlineEntity(
            event,
            thickness,
            color,
            false
        )
    }

    private fun hypot(x: Double, y: Double, d: Double): Double = sqrt(x * x + y * y + d * d)
}