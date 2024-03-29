package me.odin.features.impl.floor7.p3

import cc.polyfrost.oneconfig.libs.universal.UMatrixStack
import me.odin.events.impl.BlockChangeEvent
import me.odin.features.Category
import me.odin.features.Module
import me.odin.features.settings.impl.BooleanSetting
import me.odin.ui.clickgui.util.ColorUtil.withAlpha
import me.odin.utils.render.Color
import me.odin.utils.render.world.RenderUtils
import me.odin.utils.skyblock.dungeon.DungeonUtils
import net.minecraft.block.BlockButtonStone
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.init.Blocks
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object SimonSays : Module(
    name = "Simon Says",
    description = "Different features for the Simon Says puzzle in f7/m7.",
    category = Category.FLOOR7,
    tag = TagType.NEW
) {
    private val solver: Boolean by BooleanSetting("Solver")

    private val firstButton = BlockPos(110, 121, 91)
    private val clickInOrder = ArrayList<BlockPos>()
    private var clickNeeded = 0

    @SubscribeEvent
    fun onBlockChange(event: BlockChangeEvent) {
        if (DungeonUtils.getPhase() != 3) return
        val pos = event.pos
        val old = event.old
        val state = event.update

        if (pos == firstButton && state.block == Blocks.stone_button && state.getValue(BlockButtonStone.POWERED)) {
            clickInOrder.clear()
            clickNeeded = 0
            return
        }

        if (pos.y !in 120..123 || pos.z !in 92..95) return

        if (pos.x == 111 && state.block == Blocks.sea_lantern && !clickInOrder.contains(pos)) {
            clickInOrder.add(pos)
        } else if (pos.x == 110) {
            if (state.block == Blocks.air) {
                clickNeeded = 0
                clickInOrder.clear() // TODO: Make sure this works most of the time, or maybe add a setting for this as this will provide less consistency, but work better with "ss skip"
            } else if (state.block == Blocks.stone_button && old.block == Blocks.stone_button && state.getValue(BlockButtonStone.POWERED)) {
                val index = clickInOrder.indexOf(pos.add(1, 0, 0)) + 1
                clickNeeded = if (index >= clickInOrder.size) 0 else index
            }
        }
    }



    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        if (clickNeeded >= clickInOrder.size || !solver) return
        GlStateManager.disableCull()
        val (viewerX, viewerY, viewerZ) = RenderUtils.viewerPos

        for (index in clickNeeded until clickInOrder.size) {
            val matrixStack = UMatrixStack()
            val pos = clickInOrder[index]
            val x = pos.x - viewerX - .125
            val y = pos.y - viewerY + .3125
            val z = pos.z - viewerZ + .25
            val color = when (index) {
                clickNeeded -> Color(0, 170, 0)
                clickNeeded + 1 -> Color(255, 170, 0)
                else -> Color(170, 0, 0)
            }.withAlpha(.5f)
            RenderUtils.drawFilledBox(matrixStack, AxisAlignedBB(x, y, z, x + .1875, y + .375, z + .5), color)
        }
        GlStateManager.enableCull()
    }

    @SubscribeEvent
    fun onWorldChange(event: WorldEvent.Load) {
        clickInOrder.clear()
        clickNeeded = 0
    }
}