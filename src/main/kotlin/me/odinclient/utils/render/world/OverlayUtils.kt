package me.odinclient.utils.render.world

import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.WorldRenderer
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import kotlin.math.roundToInt

object OverlayUtils {


    private val tessellator: Tessellator = Tessellator.getInstance()
    private val worldRenderer: WorldRenderer = tessellator.worldRenderer

    fun renderDurabilityBar(x: Int, y: Int, percentFilled: Double) {
        val percent = percentFilled.coerceIn(0.0, 1.0)
        if (percent == 0.0) return
        val barWidth = (percentFilled * 13.0).roundToInt()
        val barColorIndex = (percentFilled * 255.0).roundToInt()
        GlStateManager.disableLighting()
        GlStateManager.disableDepth()
        GlStateManager.disableTexture2D()
        GlStateManager.disableAlpha()
        GlStateManager.disableBlend()
        draw(worldRenderer, x + 2, y + 13, 13, 2, 0, 0, 0, 255)
        draw(worldRenderer, x + 2, y + 13, 12, 1, (255 - barColorIndex) / 4, 64, 0, 255)
        draw(worldRenderer, x + 2, y + 13, barWidth, 1, 255 - barColorIndex, barColorIndex, 0, 255)
        GlStateManager.enableAlpha()
        GlStateManager.enableTexture2D()
        GlStateManager.enableLighting()
        GlStateManager.enableDepth()
    }

    private fun draw(renderer: WorldRenderer, x: Int, y: Int, width: Int, height: Int, red: Int, green: Int, blue: Int, alpha: Int) {
        renderer.begin(7, DefaultVertexFormats.POSITION_COLOR)
        renderer.pos((x + 0).toDouble(), (y + 0).toDouble(), 0.0).color(red, green, blue, alpha).endVertex()
        renderer.pos((x + 0).toDouble(), (y + height).toDouble(), 0.0).color(red, green, blue, alpha).endVertex()
        renderer.pos((x + width).toDouble(), (y + height).toDouble(), 0.0).color(red, green, blue, alpha).endVertex()
        renderer.pos((x + width).toDouble(), (y + 0).toDouble(), 0.0).color(red, green, blue, alpha).endVertex()
        Tessellator.getInstance().draw()
    }
}