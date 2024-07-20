package me.odinmain.features.impl.render

import com.github.stivais.ui.color.Color
import me.odinmain.features.Module
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.*
import me.odinmain.utils.render.Renderer
import me.odinmain.utils.skyblock.getBlockAt
import net.minecraft.block.material.Material
import net.minecraft.util.MovingObjectPosition.MovingObjectType
import net.minecraftforge.client.event.DrawBlockHighlightEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object BlockOverlay : Module(
    name = "Block Overlay",
    description = "Lets you customize the vanilla block overlay",
) {
    private val style by SelectorSetting("Style", Renderer.DEFAULT_STYLE, Renderer.styles, description = Renderer.STYLE_DESCRIPTION)
    private val color by ColorSetting("Color", Color.RGB(0, 0, 0, 0.4f), allowAlpha = true, description = "The color of the box.")
    private val lineWidth by NumberSetting("Line Width", 2f, 0.1f, 10f, 0.1f, description = "The width of the box's lines.")
    private val depthCheck by BooleanSetting("Depth check", false, description = "Boxes show through walls.")
    private val lineSmoothing by BooleanSetting("Line Smoothing", false, description = "Makes the lines smoother.").withDependency { style == 1 || style == 2 }

    @SubscribeEvent
    fun onRenderBlockOverlay(event: DrawBlockHighlightEvent) {
        if (event.target.typeOfHit != MovingObjectType.BLOCK || mc.gameSettings.thirdPersonView != 0) return
        event.isCanceled = true

        val blockPos = event.target.blockPos

        if (getBlockAt(blockPos).material === Material.air || !mc.theWorld.worldBorder.contains(blockPos)) return

        Renderer.drawStyledBlock(blockPos, color, style, lineWidth, depthCheck, lineSmoothing)
    }
}