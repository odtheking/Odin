package me.odinclient.features.impl.render

import me.odinclient.features.Category
import me.odinclient.features.Module
import me.odinclient.features.settings.impl.BooleanSetting
import me.odinclient.features.settings.impl.ColorSetting
import me.odinclient.ui.clickgui.util.ColorUtil.withAlpha
import me.odinclient.utils.render.Color
import me.odinclient.utils.render.world.RenderUtils
import net.minecraft.init.Blocks
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.event.entity.player.PlayerInteractEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object ClickedChests : Module(
    name = "Clicked Chests",
    category = Category.RENDER,
    description = "Draws a box around all the chests you have clicked.",
    tag = TagType.NEW
) {
    private val chests = mutableSetOf<BlockPos>()
    private val color: Color by ColorSetting("Color", Color.GOLD.withAlpha(.5f), description = "The color of the box.")
    private val filled: Boolean by BooleanSetting("Filled", true, description = "Whether or not the box should be filled.")

    @SubscribeEvent
    fun onInteract(event: PlayerInteractEvent) {
        if (event.action != PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK || mc.theWorld.getBlockState(event.pos) != Blocks.chest.defaultState) return
        chests.add(event.pos)
    }

    @SubscribeEvent
    fun onWorldLoad(event: WorldEvent.Load) {
        chests.clear()
    }

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        if (chests.isEmpty()) return
        chests.forEach {
            if (filled)
                RenderUtils.drawFilledBox(it.chestAABB, color)
            else
                RenderUtils.drawCustomESPBox(
                    it.x + .125, .75,
                    it.y.toDouble(), .875,
                    it.z + .125, .125,
                    color,
                    3f,
                    true
                )
        }
    }

    private val BlockPos.chestAABB get() = AxisAlignedBB.fromBounds(x.toDouble() + .125, y.toDouble(), z.toDouble() + .125, x + .75, y + .875, z + .75)
}