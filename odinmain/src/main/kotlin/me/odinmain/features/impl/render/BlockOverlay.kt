package me.odinmain.features.impl.render

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.*
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.RenderUtils.outlineBounds
import me.odinmain.utils.render.Renderer
import me.odinmain.utils.skyblock.getBlockAt
import me.odinmain.utils.toAABB
import net.minecraft.block.material.Material
import net.minecraft.util.MovingObjectPosition.MovingObjectType
import net.minecraftforge.client.event.DrawBlockHighlightEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object BlockOverlay : Module(
    name = "Block Overlay",
    category = Category.RENDER,
    description = "Lets you customize the vanilla block overlay",
) {
    private var fullBlock: Boolean by BooleanSetting("Full Block", false)
    private var expand: Float by NumberSetting("Expand", 0f, 0f, 10f, 0.1f)
    private val style: Int by SelectorSetting("Style", Renderer.defaultStyle, Renderer.styles, description = Renderer.styleDesc)
    private val color: Color by ColorSetting("Color", Color(0, 0, 0, 0.4f), allowAlpha = true, description = "The color of the box.")
    private val lineWidth: Float by NumberSetting("Line Width", 2f, 0.1f, 10f, 0.1f, description = "The width of the box's lines.")
    private val depthCheck: Boolean by BooleanSetting("Depth check", false, description = "Boxes show through walls.")

    @SubscribeEvent
    fun onRenderBlockOverlay(event: DrawBlockHighlightEvent) {
        if (event.target.typeOfHit != MovingObjectType.BLOCK || mc.gameSettings.thirdPersonView != 0) return
        event.isCanceled = true

        val blockPos = event.target.blockPos
        val block = getBlockAt(event.target.blockPos)

        if (block.material === Material.air || !mc.theWorld.worldBorder.contains(blockPos)) return
        block.setBlockBoundsBasedOnState(mc.theWorld, blockPos)

        val aabb = if (fullBlock) blockPos.toAABB().expand(-0.008 + expand / 1000.0, -0.008 + expand / 1000.0, -0.008 + expand / 1000.0) else
            block.getSelectedBoundingBox(mc.theWorld, blockPos).outlineBounds().expand(expand / 1000.0, expand / 1000.0, expand / 1000.0) ?: return
        Renderer.drawStyledBox(aabb, color, style, lineWidth, depthCheck)
    }
}