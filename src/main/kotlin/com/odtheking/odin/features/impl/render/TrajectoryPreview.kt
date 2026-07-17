package com.odtheking.odin.features.impl.render

import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.clickgui.settings.impl.ColorSetting
import com.odtheking.odin.clickgui.settings.impl.NumberSetting
import com.odtheking.odin.events.RenderEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.ProjectileSim
import com.odtheking.odin.utils.render.drawFilledBox
import com.odtheking.odin.utils.render.drawLine
import com.odtheking.odin.utils.render.drawWireFrameBox
import net.minecraft.world.item.BowItem
import net.minecraft.world.item.EggItem
import net.minecraft.world.item.EnderpearlItem
import net.minecraft.world.item.FishingRodItem
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.SnowballItem
import net.minecraft.world.item.ThrowablePotionItem
import net.minecraft.world.phys.AABB

object TrajectoryPreview : Module(
    name = "Trajectory Preview",
    description = "Renders the predicted flight path of held projectiles. Vanilla adds random spread, so real impacts can differ slightly."
) {
    private val arrows by BooleanSetting("Arrows", true, desc = "Preview bow arrows. Assumes full charge unless actively drawing.")
    private val pearls by BooleanSetting("Ender Pearls", true, desc = "Preview ender pearls.")
    private val snowballs by BooleanSetting("Snowballs & Eggs", false, desc = "Preview snowballs and eggs.")
    private val fishingRod by BooleanSetting("Fishing Rod", false, desc = "Preview fishing rod casts.")
    private val potions by BooleanSetting("Potions", false, desc = "Preview splash and lingering potions.")
    private val lineColor by ColorSetting("Line Color", Colors.WHITE, true, desc = "Color of the trajectory line.")
    private val impactColor by ColorSetting("Impact Color", Colors.MINECRAFT_RED, true, desc = "Color of the impact marker.")
    private val entityColor by ColorSetting("Entity Hit Color", Colors.MINECRAFT_YELLOW, true, desc = "Color of the entity hit highlight.")
    private val lineWidth by NumberSetting("Line Width", 3f, 1, 5, 0.5, desc = "Thickness of the trajectory line.")
    private val throughWalls by BooleanSetting("Through Walls", false, desc = "Renders the preview through blocks.")

    init {
        on<RenderEvent.Extract> {
            val player = mc.player ?: return@on
            val stack = player.mainHandItem
            if (!typeEnabled(stack)) return@on
            val partialTicks = mc.deltaTracker.getGameTimeDeltaPartialTick(true)
            val launch = ProjectileSim.launchFor(stack, player, partialTicks) ?: return@on
            val result = ProjectileSim.simulate(player, launch)
            if (result.points.size < 2) return@on

            drawLine(result.points, lineColor, depth = !throughWalls, thickness = lineWidth)
            result.blockHit?.let {
                drawFilledBox(AABB.ofSize(it.location, 0.25, 0.25, 0.25), impactColor, depth = !throughWalls)
            }
            result.entityHit?.let {
                drawWireFrameBox(it.boundingBox, entityColor, depth = !throughWalls)
            }
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
