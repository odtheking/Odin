package me.odinclient.features.impl.render

import me.odinclient.features.Category
import me.odinclient.features.Module
import me.odinclient.features.settings.impl.BooleanSetting
import me.odinclient.features.settings.impl.ColorSetting
import me.odinclient.ui.clickgui.util.ColorUtil.withAlpha
import me.odinclient.utils.Utils.equalsOneOf
import me.odinclient.utils.render.Color
import me.odinclient.utils.render.world.RenderUtils
import net.minecraft.block.BlockLever
import net.minecraft.init.Blocks
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.event.entity.player.PlayerInteractEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object ClickedChests : Module(
    name = "Clicked Chests",
    category = Category.RENDER,
    description = "Draws a box around all the chests you have clicked.",
    tag = TagType.NEW
) {
    private val chests = mutableSetOf<BlockPos>()
    private val color: Color by ColorSetting("Color", Color.GOLD.withAlpha(.4f), allowAlpha = true, description = "The color of the box.")
    private val filled: Boolean by BooleanSetting("Filled", true, description = "Whether or not the box should be filled.")
    private val phase: Boolean by BooleanSetting("Phase", true, description = "Boxes show through walls.")

    @SubscribeEvent
    fun onInteract(event: PlayerInteractEvent) {
        if (event.action != PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK || !mc.theWorld.getBlockState(event.pos).block.equalsOneOf(Blocks.chest, Blocks.trapped_chest)) return
        chests.add(event.pos)
        (mc.theWorld.getBlockState(event.pos) as BlockLever).blockBoundsMaxY
    }

    init { onWorldLoad { chests.clear() } }

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        if (chests.isEmpty()) return
        chests.forEach {
            if (filled) {
                val (viewerX, viewerY, viewerZ) = RenderUtils.viewerPos
                val x = it.x - viewerX + .0625
                val y = it.y - viewerY
                val z = it.z - viewerZ + .0625
                RenderUtils.drawFilledBox(AxisAlignedBB(x, y, z, x + .875, y + 0.875, z + 0.875), color, phase)
            } else
                RenderUtils.drawCustomESPBox(
                    it.x + .0625, .875,
                    it.y.toDouble(), .875,
                    it.z + .0625, .875,
                    color,
                    3f,
                    phase
                )
        }
    }
}