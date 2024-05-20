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
    private var disableDepth: Boolean by BooleanSetting("Disable Depth", false)
    private var lineSmoothing: Boolean by BooleanSetting("Line Smoothing", false)
    private var lineWidth: Float by NumberSetting("Line Width", 2f, 0.1f, 10f, 0.1f)
    private var expand: Float by NumberSetting("Expand", 0f, 0f, 10f, 0.1f)
    private var color: Color by ColorSetting("Color", Color(0, 0, 0, 0.4f), allowAlpha = true)
    private val style: Int by SelectorSetting("Style", "Filled", arrayListOf("Filled", "Outline", "Filled Outline"), description = "Whether or not the box should be filled.")
    private val reset: () -> Unit by ActionSetting("Reset") {
        fullBlock = false
        disableDepth = false
        lineSmoothing = false
        lineWidth = 2f
        expand = 0f
        color = Color(0, 0, 0, 0.4f)
    }

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

        Renderer.drawBox(aabb, color, lineWidth, depth = disableDepth, outlineAlpha = if (style == 0) 0 else color.alpha, fillAlpha = if (style == 1) 0 else color.alpha)
    }
}