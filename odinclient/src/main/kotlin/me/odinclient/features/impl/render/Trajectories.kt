package me.odinclient.features.impl.render

import me.odinmain.events.impl.RenderEntityModelEvent
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.*
import me.odinmain.ui.clickgui.util.ColorUtil.withAlpha
import me.odinmain.utils.addVec
import me.odinmain.utils.render.*
import me.odinmain.utils.render.RenderUtils.renderX
import me.odinmain.utils.render.RenderUtils.renderY
import me.odinmain.utils.render.RenderUtils.renderZ
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.skyblock.isHolding
import me.odinmain.utils.skyblock.isShortbow
import net.minecraft.entity.Entity
import net.minecraft.entity.boss.EntityWither
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.monster.EntityBlaze
import net.minecraft.entity.projectile.EntityArrow
import net.minecraft.item.ItemBow
import net.minecraft.item.ItemEnderPearl
import net.minecraft.util.*
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.math.sqrt

object Trajectories : Module(
    name = "Trajectories",
    description = "Displays the trajectory of pearls and bows.",
    category = Category.RENDER
) {
    private val bows: Boolean by BooleanSetting("Bows", true, description = "Render trajectories of bow arrows")
    private val pearls: Boolean by BooleanSetting("Pearls", true, description = "Render trajectories of ender pearls")
    private val plane: Boolean by BooleanSetting("Show Plane", false, description = "Shows a flat square rotated relative to the predicted block that will be hit.")
    private val boxes: Boolean by BooleanSetting("Show Boxes", true, description = "Shows boxes displaying where arrows or pearls will hit, if this is disabled it will only highlight entities your arrows will hit.")
    private val lines: Boolean by BooleanSetting("Show Lines", true, description = "Shows the trajectory as a line.")
    private val range: Float by NumberSetting("Solver Range", 30f, 1f, 60f, 1f, description = "How many ticks are simulated, performance impact scales with this")
    private val width: Float by NumberSetting("Line Width", 1f, 0.1f, 5.0, 0.1f)
    private val planeSize: Float by NumberSetting("Plane Size", 2f, 0.1f, 5.0, 0.1f).withDependency { plane }
    private val boxSize: Float by NumberSetting("Box Size", 0.5f, 0.5f, 3.0f, 0.1f).withDependency { boxes }
    private val color: Color by ColorSetting("Color", Color.CYAN, true)

    private var boxRenderQueue: MutableList<Pair<Vec3, Vec3>> = mutableListOf()
    private var entityRenderQueue = mutableListOf<Entity>()
    private var pearlImpactPos: Pair<Vec3,Vec3>? = null
    private var planePos: MovingObjectPosition? = null

    @SubscribeEvent
    fun onRenderWorldLast(event: RenderWorldLastEvent) {
        entityRenderQueue.clear()
        planePos = null
        if (mc.thePlayer == null || mc.thePlayer.heldItem == null) return
        if (bows && mc.thePlayer.heldItem.item is ItemBow) {
            val pair1: Pair<ArrayList<Vec3>, MovingObjectPosition?>
            var pair2: Pair<ArrayList<Vec3>, MovingObjectPosition?>
            var pair3: Pair<ArrayList<Vec3>, MovingObjectPosition?>
            if (mc.thePlayer.heldItem?.isShortbow == true) {
                pair1 = this.setBowTrajectoryHeading(0f, false)
                pair2 = Pair(arrayListOf(Vec3(0.0, 0.0, 0.0)), null)
                pair3 = Pair(arrayListOf(Vec3(0.0, 0.0, 0.0)), null)
                if (isHolding("TERMINATOR")) {
                    pair2 = this.setBowTrajectoryHeading(-5f, false)
                    pair3 = this.setBowTrajectoryHeading(5f, false)
                }
            } else {
                if (mc.thePlayer.itemInUseDuration == 0) return
                pair1 = this.setBowTrajectoryHeading(0f, true)
                pair2 = Pair(arrayListOf(Vec3(0.0, 0.0, 0.0)), null)
                pair3 = Pair(arrayListOf(Vec3(0.0, 0.0, 0.0)), null)
            }
            if (boxes) this.drawBowCollisionBoxes()
            if (plane) {
                this.drawPlaneCollision(pair1.second)
                this.drawPlaneCollision(pair2.second)
                this.drawPlaneCollision(pair3.second)
            }
            if (lines) {
                this.drawLine(pair1.first)
                this.drawLine(pair2.first)
                this.drawLine(pair3.first)
            }
        }
        if (pearls) {
            pearlImpactPos = null
            val itemStack = mc.thePlayer.heldItem
            if (itemStack?.item is ItemEnderPearl && !itemStack.displayName.contains("leap", ignoreCase = true)) {
                val pair = this.setPearlTrajectoryHeading()
                if (boxes) this.drawPearlCollisionBox()
                if (plane) this.drawPlaneCollision(pair.second)
                if (lines) this.drawLine(pair.first)
            }
        }
    }

    private fun setPearlTrajectoryHeading(): Pair<ArrayList<Vec3>, MovingObjectPosition?> {
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

        return calculatePearlTrajectory(Vec3(motionX, motionY, motionZ), Vec3(posX, posY, posZ))
    }

    private fun calculatePearlTrajectory(mV: Vec3,pV: Vec3): Pair<ArrayList<Vec3>, MovingObjectPosition?> {
        var hitResult = false
        var motionVec = mV
        var posVec = pV
        val lines = arrayListOf<Vec3>()
        var rayTraceHit: MovingObjectPosition? = null
        for (i in 0..range.toInt()) {
            if (hitResult) break
            lines.add(posVec)
            val vec = motionVec.add(posVec)
            val rayTrace = mc.theWorld.rayTraceBlocks(posVec, vec, false, true, false)
            if (rayTrace != null) {
                rayTraceHit = rayTrace
                lines.add(rayTrace.hitVec)
                pearlImpactPos =
                    Pair(
                        rayTrace.hitVec.addVector(-0.15 * boxSize, -0.15 * boxSize, -0.15 * boxSize),
                        rayTrace.hitVec.addVector(0.15 * boxSize, 0.15 * boxSize, 0.15 * boxSize)
                    )
                hitResult = true
            }
            posVec = posVec.add(motionVec)
            motionVec = Vec3(motionVec.xCoord * 0.99, motionVec.yCoord * 0.99 - 0.03, motionVec.zCoord * 0.99)
        }
        return Pair(lines, rayTraceHit)
    }

    private fun setBowTrajectoryHeading(yawOffset: Float, bowCharge: Boolean): Pair<ArrayList<Vec3>, MovingObjectPosition?> {
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

        return calculateBowTrajectory(Vec3(motionX, motionY, motionZ), Vec3(posX, posY, posZ))
    }

    private fun calculateBowTrajectory(mV: Vec3,pV: Vec3): Pair<ArrayList<Vec3>, MovingObjectPosition?> {
        var hitResult = false
        var motionVec = mV
        var posVec = pV
        val lines = arrayListOf<Vec3>()
        var rayTraceHit: MovingObjectPosition? = null
        for (i in 0..range.toInt()) {
            if (hitResult) break
            lines.add(posVec)
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
                rayTraceHit = rayTrace
                lines.add(rayTrace.hitVec)
                boxRenderQueue.add(
                    Pair(
                        rayTrace.hitVec.addVector(-0.15 * boxSize, -0.15 * boxSize, -0.15 * boxSize),
                        rayTrace.hitVec.addVector(0.15 * boxSize, 0.15 * boxSize, 0.15 * boxSize)
                    )
                )
                hitResult = true
            }
            posVec = posVec.add(motionVec)
            motionVec = Vec3(motionVec.xCoord * 0.99, motionVec.yCoord * 0.99 - 0.05, motionVec.zCoord * 0.99)
        }
        return Pair(lines, rayTraceHit)
    }

    private fun drawPlaneCollision(rayTrace: MovingObjectPosition?) {
        val vec1: Vec3
        val vec2: Vec3
        when (rayTrace?.sideHit) {
            EnumFacing.DOWN, EnumFacing.UP -> {
                vec1 = rayTrace.hitVec.addVec(-0.15 * planeSize, -0.02, -0.15 * planeSize)
                vec2 = rayTrace.hitVec.addVec(0.15 * planeSize, 0.02, 0.15 * planeSize)
            }
            EnumFacing.NORTH, EnumFacing.SOUTH -> {
                vec1 = rayTrace.hitVec.addVec(-0.15 * planeSize, -0.15 * planeSize, -0.02)
                vec2 = rayTrace.hitVec.addVec(0.15 * planeSize, 0.15 * planeSize, 0.02)
            }
            EnumFacing.WEST, EnumFacing.EAST -> {
                vec1 = rayTrace.hitVec.addVec(-0.02, -0.15 * planeSize, -0.15 * planeSize)
                vec2 = rayTrace.hitVec.addVec(0.02, 0.15 * planeSize, 0.15 * planeSize)
            }
            null -> return
        }
        val aabb = AxisAlignedBB(vec1.xCoord, vec1.yCoord, vec1.zCoord, vec2.xCoord, vec2.yCoord, vec2.zCoord)
        RenderUtils.drawFilledAABB(aabb, color.withAlpha(color.alpha / 2, true), false)
    }

    private fun drawLine(lines: ArrayList<Vec3>) {
        if (lines.isEmpty()) return

        Renderer.draw3DLine(*lines.toTypedArray(), color = color, lineWidth = width, depth = true)
    }

    private fun drawPearlCollisionBox() {
        if (pearlImpactPos == null) return
        val pos = pearlImpactPos ?: return
        val aabb = AxisAlignedBB(
            pos.first.xCoord, pos.first.yCoord, pos.first.zCoord,
            pos.second.xCoord, pos.second.yCoord, pos.second.zCoord
        )
        Renderer.drawBox(aabb, color, width, depth = false, fillAlpha = 0)

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
            val aabb = AxisAlignedBB(
                b.first.xCoord, b.first.yCoord, b.first.zCoord,
                b.second.xCoord, b.second.yCoord, b.second.zCoord
            )
            Renderer.drawBox(aabb, color, width, depth = true, fillAlpha = 0)
        }
        boxRenderQueue.clear()
    }

    @SubscribeEvent
    fun onRenderModel(event: RenderEntityModelEvent) {
        val player = mc.thePlayer ?: return
        val entity = event.entity

        if (entity !in entityRenderQueue ||
            !player.canEntityBeSeen(entity) ||
            (!bows || player.heldItem?.item !is ItemBow) ||
            (entity is EntityBlaze && DungeonUtils.inDungeons) ||
            (entity is EntityWither && entity.isInvisible)
            ) return

        OutlineUtils.outlineEntity(event, color, width)
    }

    private fun hypot(x: Double, y: Double, d: Double): Double = sqrt(x * x + y * y + d * d)
}