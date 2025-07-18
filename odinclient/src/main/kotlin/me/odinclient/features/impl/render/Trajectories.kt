package me.odinclient.features.impl.render

import me.odinmain.clickgui.settings.Setting.Companion.withDependency
import me.odinmain.clickgui.settings.impl.BooleanSetting
import me.odinmain.clickgui.settings.impl.ColorSetting
import me.odinmain.clickgui.settings.impl.NumberSetting
import me.odinmain.events.impl.RenderEntityModelEvent
import me.odinmain.features.Module
import me.odinmain.utils.*
import me.odinmain.utils.render.Color.Companion.multiplyAlpha
import me.odinmain.utils.render.Colors
import me.odinmain.utils.render.OutlineUtils
import me.odinmain.utils.render.RenderUtils
import me.odinmain.utils.render.RenderUtils.renderVec
import me.odinmain.utils.render.Renderer
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.skyblock.isHolding
import me.odinmain.utils.skyblock.isLeap
import me.odinmain.utils.skyblock.isShortbow
import net.minecraft.entity.Entity
import net.minecraft.entity.boss.EntityWither
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.monster.EntityBlaze
import net.minecraft.entity.projectile.EntityArrow
import net.minecraft.item.ItemBow
import net.minecraft.item.ItemEnderPearl
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.EnumFacing
import net.minecraft.util.MovingObjectPosition
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import kotlin.math.cos
import kotlin.math.sin

object Trajectories : Module(
    name = "Trajectories",
    description = "Displays the trajectory of pearls and bows."
) {
    private val bows by BooleanSetting("Bows", true, desc = "Render trajectories of bow arrows.")
    private val pearls by BooleanSetting("Pearls", true, desc = "Render trajectories of ender pearls.")
    private val plane by BooleanSetting("Show Plane", false, desc = "Shows a flat square rotated relative to the predicted block that will be hit.")
    private val boxes by BooleanSetting("Show Boxes", true, desc = "Shows boxes displaying where arrows or pearls will hit, if this is disabled it will only highlight entities your arrows will hit.")
    private val lines by BooleanSetting("Show Lines", true, desc = "Shows the trajectory as a line.")
    private val range by NumberSetting("Solver Range", 30, 1, 120, 1, desc = "How many ticks are simulated, performance impact scales with this.")
    private val width by NumberSetting("Line Width", 1f, 0.1f, 5.0, 0.1f, desc = "The width of the line.")
    private val planeSize by NumberSetting("Plane Size", 2f, 0.1f, 5.0, 0.1f, desc = "The size of the plane.").withDependency { plane }
    private val boxSize by NumberSetting("Box Size", 0.5f, 0.5f, 3.0f, 0.1f, desc = "The size of the box.").withDependency { boxes }
    private val color by ColorSetting("Color", Colors.MINECRAFT_DARK_AQUA, true, desc = "The color of the trajectory.")
    private val depth by BooleanSetting("Depth Check", true, desc = "Whether or not to depth check the trajectory.")

    private var boxRenderQueue: MutableList<AxisAlignedBB> = mutableListOf()
    private var entityRenderQueue = mutableListOf<Entity>()
    private var pearlImpactPos: AxisAlignedBB? = null
    private var planePos: MovingObjectPosition? = null
    private var charge = 0f
    private var lastCharge = 0f

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (event.phase == TickEvent.Phase.END) {
            lastCharge = charge
            charge = minOf((72000 - (mc.thePlayer?.itemInUseCount ?: 0)) / 20f, 1.0f) * 2f
        }
        if ((lastCharge - charge) > 1f) lastCharge = charge
    }

    @SubscribeEvent
    fun onRenderWorldLast(event: RenderWorldLastEvent) {
        entityRenderQueue.clear()
        planePos = null
        val heldItem = mc.thePlayer?.heldItem ?: return

        if (bows && heldItem.item is ItemBow) {
            val pairs = if (heldItem.isShortbow) {
                listOfNotNull(
                    calculateTrajectory(0f, false),
                    if (isHolding("TERMINATOR")) calculateTrajectory(-5f, false) else null,
                    if (isHolding("TERMINATOR")) calculateTrajectory(5f, false) else null
                )
            } else {
                if (mc.thePlayer?.itemInUseDuration != 0) return
                listOf(calculateTrajectory(0f, isPearl = false, useCharge = true))
            }

            if (boxes) drawCollisionBoxes(isPearl = false)
            pairs.forEach { pair ->
                if (plane) pair.second?.let { drawPlaneCollision(it)  }
                if (lines) Renderer.draw3DLine(pair.first, color, width, depth)
            }
        }

        if (pearls && heldItem.item is ItemEnderPearl && !heldItem.isLeap) {
            val pair = calculateTrajectory(0f, isPearl = true)
            if (boxes) drawCollisionBoxes(isPearl = true)
            if (lines) Renderer.draw3DLine(pair.first, color, width, depth)
            if (plane) pair.second?.let { drawPlaneCollision(it)  }
        }
    }

    private fun calculateTrajectory(yawOffset: Float, isPearl: Boolean, useCharge: Boolean = false): Pair<ArrayList<Vec3>, MovingObjectPosition?> {
        val yaw = Math.toRadians((mc.thePlayer.rotationYaw + yawOffset).toDouble())
        val offset = Vec3(-cos(yaw) * 0.16, mc.thePlayer.eyeHeight - 0.1, -sin(yaw) * 0.16)
        var pos = mc.thePlayer.renderVec.add(offset)

        val velocityMultiplier = if (isPearl) 1.5f
        else (if (!useCharge) 2f else lastCharge + (charge - lastCharge) * RenderUtils.partialTicks) * 1.5f

        var motion = getLook().normalize().multiply(velocityMultiplier)

        var hitResult = false
        val lines = arrayListOf<Vec3>()
        var rayTraceHit: MovingObjectPosition? = null

        repeat(range + 1) {
            if (hitResult) return@repeat
            lines.add(pos)

            if (!isPearl) {

                val aabb = pos.add(motion).toAABB(1.01)
                val entityHit = mc.theWorld?.getEntitiesWithinAABBExcludingEntity(mc.thePlayer, aabb)
                    ?.filter { it !is EntityArrow && it !is EntityArmorStand }.orEmpty()
                if (entityHit.isNotEmpty()) {
                    hitResult = true
                    entityRenderQueue.addAll(entityHit)
                    return@repeat
                }
            }

            mc.theWorld?.rayTraceBlocks(pos, motion.add(pos), false, true, false)?.let {
                rayTraceHit = it
                lines.add(it.hitVec)
                if (boxes) {
                    val box = AxisAlignedBB(
                        it.hitVec.xCoord - 0.15 * boxSize, it.hitVec.yCoord - 0.15 * boxSize, it.hitVec.zCoord - 0.15 * boxSize,
                        it.hitVec.xCoord + 0.15 * boxSize, it.hitVec.yCoord + 0.15 * boxSize, it.hitVec.zCoord + 0.15 * boxSize
                    )
                    if (isPearl) pearlImpactPos = box
                    else boxRenderQueue.add(box)
                }
                hitResult = true
            }

            pos = pos.add(motion)
            motion = if (isPearl) Vec3(motion.xCoord * 0.99, motion.yCoord * 0.99 - 0.03, motion.zCoord * 0.99)
            else Vec3(motion.xCoord * 0.99, motion.yCoord * 0.99 - 0.05, motion.zCoord * 0.99)
        }

        return lines to rayTraceHit
    }

    private fun drawPlaneCollision(hit: MovingObjectPosition) {
        val (vec1, vec2) = when (hit.sideHit) {
            EnumFacing.DOWN, EnumFacing.UP ->
                hit.hitVec.addVec(-0.15 * planeSize, -0.02, -0.15 * planeSize) to
                        hit.hitVec.addVec(0.15 * planeSize, 0.02, 0.15 * planeSize)
            EnumFacing.NORTH, EnumFacing.SOUTH ->
                hit.hitVec.addVec(-0.15 * planeSize, -0.15 * planeSize, -0.02) to
                        hit.hitVec.addVec(0.15 * planeSize, 0.15 * planeSize, 0.02)
            EnumFacing.WEST, EnumFacing.EAST ->
                hit.hitVec.addVec(-0.02, -0.15 * planeSize, -0.15 * planeSize) to
                        hit.hitVec.addVec(0.02, 0.15 * planeSize, 0.15 * planeSize)
            else -> return
        }
        RenderUtils.drawFilledAABB(AxisAlignedBB(vec1.xCoord, vec1.yCoord, vec1.zCoord, vec2.xCoord, vec2.yCoord, vec2.zCoord), color.multiplyAlpha(0.5f), depth)
    }

    private fun drawCollisionBoxes(isPearl: Boolean) {
        if (isPearl) {
            pearlImpactPos?.let { aabb ->
                Renderer.drawBox(aabb, color, width, depth = depth, fillAlpha = 0)
                pearlImpactPos = null
            }
        } else {
            if (boxRenderQueue.isEmpty()) return
            val renderVec = mc.thePlayer?.renderVec ?: return
            for (axisAlignedBB in boxRenderQueue) {
                if (axisAlignedBB.middle.distanceTo(getPositionEyes(renderVec)) < 2) {
                    boxRenderQueue.clear()
                    return
                }
                Renderer.drawBox(axisAlignedBB, color, width, depth = depth, fillAlpha = 0)
            }
            boxRenderQueue.clear()
        }
    }

    @SubscribeEvent
    fun onRenderModel(event: RenderEntityModelEvent) {
        val player = mc.thePlayer ?: return
        val entity = event.entity

        if (entity !in entityRenderQueue ||
            !player.canEntityBeSeen(entity) ||
            !bows || player.heldItem?.item !is ItemBow ||
            (entity is EntityBlaze && DungeonUtils.inDungeons) ||
            (entity is EntityWither && entity.isInvisible)
        ) return

        OutlineUtils.outlineEntity(event, color, width, depth)
    }

}