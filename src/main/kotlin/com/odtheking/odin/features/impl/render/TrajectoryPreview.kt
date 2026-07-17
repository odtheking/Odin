package com.odtheking.odin.features.impl.render

import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.clickgui.settings.impl.ColorSetting
import com.odtheking.odin.clickgui.settings.impl.NumberSetting
import com.odtheking.odin.clickgui.settings.impl.SelectorSetting
import com.odtheking.odin.events.RenderEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.ProjectileSim
import com.odtheking.odin.utils.render.drawFilledBox
import com.odtheking.odin.utils.render.drawLine
import net.minecraft.core.Direction
import net.minecraft.world.item.BowItem
import net.minecraft.world.item.EggItem
import net.minecraft.world.item.EnderpearlItem
import net.minecraft.world.item.FishingRodItem
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.SnowballItem
import net.minecraft.world.item.ThrowablePotionItem
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.BlockHitResult

object TrajectoryPreview : Module(
    name = "Trajectory Preview",
    description = "Renders the predicted flight path of held projectiles. Vanilla adds random spread, so real impacts can differ slightly."
) {
    private const val IGNORE_FIRST_POINTS = 3 // meteor's ignore-rendering-first-ticks default

    private val arrows by BooleanSetting("Arrows", true, desc = "Preview bow arrows. Assumes full charge unless actively drawing.")
    private val pearls by BooleanSetting("Ender Pearls", true, desc = "Preview ender pearls.")
    private val snowballs by BooleanSetting("Snowballs & Eggs", false, desc = "Preview snowballs and eggs.")
    private val fishingRod by BooleanSetting("Fishing Rod", false, desc = "Preview fishing rod casts.")
    private val potions by BooleanSetting("Potions", false, desc = "Preview splash and lingering potions.")
    private val lineColor by ColorSetting("Line Color", Colors.WHITE, true, desc = "Color of the trajectory line.")
    private val impactColor by ColorSetting("Impact Color", Colors.MINECRAFT_RED, true, desc = "Color of the impact marker.")
    private val entityColor by ColorSetting("Entity Hit Color", Colors.MINECRAFT_YELLOW, true, desc = "Line color when the projectile would hit an entity.")
    private val renderStyle by SelectorSetting("Render Style", "Line", listOf("Line", "Dots", "Both"), desc = "Trajectory drawn as a line, a dotted trail, or both.")
    private val lineWidth by NumberSetting("Line Width", 5f, 1, 10, 0.5, desc = "Thickness of the trajectory line.")
    private val throughWalls by BooleanSetting("Through Walls", false, desc = "Renders the preview through blocks.")

    init {
        on<RenderEvent.Extract> {
            val player = mc.player ?: return@on
            val stack = player.mainHandItem
            if (!typeEnabled(stack)) return@on
            val partialTicks = mc.deltaTracker.getGameTimeDeltaPartialTick(true)
            val launch = ProjectileSim.launchFor(stack, player, partialTicks) ?: return@on
            val result = ProjectileSim.simulate(player, launch)

            val visiblePoints = if (result.points.size > IGNORE_FIRST_POINTS) result.points.drop(IGNORE_FIRST_POINTS) else emptyList()
            if (visiblePoints.size >= 2) {
                val pathColor = if (result.entityHit != null) entityColor else lineColor
                if (renderStyle != 1) drawLine(visiblePoints, pathColor, depth = !throughWalls, thickness = lineWidth)
                if (renderStyle != 0) {
                    val dotSize = (lineWidth * 0.02).toDouble()
                    visiblePoints.forEach { point ->
                        drawFilledBox(AABB.ofSize(point, dotSize, dotSize, dotSize), pathColor, depth = !throughWalls)
                    }
                }
            }

            result.blockHit?.let { hit ->
                drawFilledBox(impactQuad(hit), impactColor, depth = !throughWalls)
            }

            if (result.entityHit != null && result.points.isNotEmpty()) {
                drawFilledBox(AABB.ofSize(result.points.last(), 0.25, 0.25, 0.25), entityColor, depth = !throughWalls)
            }
        }
    }

    // Thin 0.5×0.5 quad lying on the hit face (meteor draws a flat hit quad, not a cube).
    private fun impactQuad(hit: BlockHitResult): AABB {
        val c = hit.location
        return when (hit.direction.axis) {
            Direction.Axis.Y -> AABB(c.x - 0.25, c.y - 0.01, c.z - 0.25, c.x + 0.25, c.y + 0.01, c.z + 0.25)
            Direction.Axis.X -> AABB(c.x - 0.01, c.y - 0.25, c.z - 0.25, c.x + 0.01, c.y + 0.25, c.z + 0.25)
            Direction.Axis.Z -> AABB(c.x - 0.25, c.y - 0.25, c.z - 0.01, c.x + 0.25, c.y + 0.25, c.z + 0.01)
        }
    }

    private fun typeEnabled(stack: ItemStack): Boolean = when (stack.item) {
        is BowItem -> arrows
        is EnderpearlItem -> pearls
        is SnowballItem, is EggItem -> snowballs
        is FishingRodItem -> fishingRod
        is ThrowablePotionItem -> potions
        else -> false
    }
}
