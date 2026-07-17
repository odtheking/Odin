package com.odtheking.odin.utils

import net.minecraft.util.Mth
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.ExperienceOrb
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.entity.projectile.Projectile
import net.minecraft.world.item.BowItem
import net.minecraft.world.item.EggItem
import net.minecraft.world.item.EnderpearlItem
import net.minecraft.world.item.FishingRodItem
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.SnowballItem
import net.minecraft.world.item.ThrowablePotionItem
import net.minecraft.world.level.ClipContext
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.HitResult
import net.minecraft.world.phys.Vec3

/**
 * Client-side forward simulation of vanilla projectile physics.
 * Constants and step order ported from maDU59's ProjectilesTrajectoryPreview (MIT).
 * The preview is the center line — vanilla adds random per-shot spread.
 */
object ProjectileSim {

    private const val DEG_TO_RAD = 0.017453292f

    // P = pos += vel, D = vel *= drag, G = vel.y -= gravity, applied in listed order each tick
    enum class StepOrder { PDG, GDP, GPD }

    enum class ProjectileType(
        val gravity: Double,
        val drag: Double,
        val waterDrag: Double,
        val order: StepOrder,
        val hitsWater: Boolean = false
    ) {
        ARROW(0.05, 0.99, 0.6, StepOrder.PDG),
        THROWN(0.03, 0.99, 0.8, StepOrder.GDP),
        POTION(0.05, 0.99, 0.8, StepOrder.GDP),
        FISHING_ROD(0.03, 0.92, 0.92, StepOrder.GPD, hitsWater = true)
    }

    data class Launch(val type: ProjectileType, val position: Vec3, val velocity: Vec3)

    data class SimResult(val points: List<Vec3>, val blockHit: BlockHitResult?, val entityHit: Entity?)

    /**
     * Maps the held item to launch parameters, or null if the item is not a supported projectile.
     * Bows use the real draw charge while drawing, otherwise assume full charge —
     * Skyblock shortbows (Terminator, Juju) always fire at full power without drawing.
     */
    fun launchFor(stack: ItemStack, player: Player, partialTicks: Float): Launch? {
        val eyePos = player.getEyePosition(partialTicks).subtract(0.0, 0.1, 0.0)
        return when (stack.item) {
            is BowItem -> {
                val charge = if (player.isUsingItem && player.useItem === stack)
                    BowItem.getPowerForTime(player.ticksUsingItem)
                else 1f
                if (charge < 0.1f) return null
                Launch(ProjectileType.ARROW, eyePos, player.getViewVector(partialTicks).scale(3.0 * charge))
            }
            is EnderpearlItem, is SnowballItem, is EggItem ->
                Launch(ProjectileType.THROWN, eyePos, player.getViewVector(partialTicks).scale(1.5))
            is ThrowablePotionItem ->
                Launch(ProjectileType.POTION, eyePos, angleFromRot(player.xRot, player.yRot, -20f).scale(0.5))
            is FishingRodItem -> {
                if (player.fishing != null) return null
                val h = Mth.cos((-player.yRot * DEG_TO_RAD - Mth.PI).toDouble())
                val i = Mth.sin((-player.yRot * DEG_TO_RAD - Mth.PI).toDouble())
                val j = -Mth.cos((-player.xRot * DEG_TO_RAD).toDouble())
                val k = Mth.sin((-player.xRot * DEG_TO_RAD).toDouble())
                val eye = player.getEyePosition(partialTicks)
                val pos = Vec3(eye.x - i * 0.3, eye.y, eye.z - h * 0.3)
                var vel = Vec3((-i).toDouble(), Mth.clamp(-(k / j), -5f, 5f).toDouble(), (-h).toDouble())
                val len = vel.length()
                vel = vel.multiply(0.6 / len + 0.5, 0.6 / len + 0.5, 0.6 / len + 0.5)
                Launch(ProjectileType.FISHING_ROD, pos, vel)
            }
            else -> null
        }
    }

    /**
     * Ticks the projectile forward until it hits a block, hits an entity,
     * falls out of the world, or maxTicks elapse.
     */
    fun simulate(player: Player, launch: Launch, maxTicks: Int = 200): SimResult {
        val level = player.level()
        val points = ArrayList<Vec3>(maxTicks + 2)
        var pos = launch.position
        var prevPos = pos
        var vel = launch.velocity.add(player.deltaMovement)
        var drag = launch.type.drag
        val gravity = launch.type.gravity

        repeat(maxTicks) {
            points.add(pos)

            when (launch.type.order) {
                StepOrder.PDG -> { pos = pos.add(vel); vel = vel.scale(drag); vel = vel.subtract(0.0, gravity, 0.0) }
                StepOrder.GDP -> { vel = vel.subtract(0.0, gravity, 0.0); vel = vel.scale(drag); pos = pos.add(vel) }
                StepOrder.GPD -> { vel = vel.subtract(0.0, gravity, 0.0); pos = pos.add(vel); vel = vel.scale(drag) }
            }

            var closestEntity: Entity? = null
            var closestDistSq = Double.MAX_VALUE
            var entityHitPos: Vec3? = null
            level.getEntitiesOfClass(Entity::class.java, AABB(prevPos, pos).inflate(1.0)) { e ->
                !e.isSpectator && e.isAlive && e !is Projectile && e !is ItemEntity && e !is ExperienceOrb && e !== player
            }.forEach { entity ->
                val clip = entity.boundingBox.inflate(entity.pickRadius.toDouble()).clip(prevPos, pos)
                if (clip.isPresent) {
                    val distSq = prevPos.distanceToSqr(clip.get())
                    if (distSq < closestDistSq) {
                        closestDistSq = distSq
                        closestEntity = entity
                        entityHitPos = clip.get()
                    }
                }
            }

            val fluid = if (launch.type.hitsWater) ClipContext.Fluid.WATER else ClipContext.Fluid.NONE
            val blockHit = level.clip(ClipContext(prevPos, pos, ClipContext.Block.COLLIDER, fluid, player))
            if (!launch.type.hitsWater) {
                drag = if (blockHit.type == HitResult.Type.MISS &&
                    level.clip(ClipContext(prevPos, pos, ClipContext.Block.COLLIDER, ClipContext.Fluid.WATER, player)).type != HitResult.Type.MISS
                ) launch.type.waterDrag else launch.type.drag
            }

            if (blockHit.type != HitResult.Type.MISS && prevPos.distanceToSqr(blockHit.location) < closestDistSq) {
                points.add(blockHit.location)
                return SimResult(points, blockHit, null)
            }
            entityHitPos?.let { hit ->
                points.add(hit)
                return SimResult(points, null, closestEntity)
            }
            if (pos.y < level.minY - 120) return SimResult(points, null, null)
            prevPos = pos
        }
        return SimResult(points, null, null)
    }

    // Direction vector from pitch/yaw with a pitch offset — vanilla thrown-potion launch uses -20°.
    private fun angleFromRot(pitch: Float, yaw: Float, pitchOffset: Float): Vec3 {
        val x = -Mth.sin((yaw * DEG_TO_RAD).toDouble()) * Mth.cos((pitch * DEG_TO_RAD).toDouble())
        val y = -Mth.sin(((pitch + pitchOffset) * DEG_TO_RAD).toDouble())
        val z = Mth.cos((yaw * DEG_TO_RAD).toDouble()) * Mth.cos((pitch * DEG_TO_RAD).toDouble())
        return Vec3(x.toDouble(), y.toDouble(), z.toDouble()).normalize()
    }
}
