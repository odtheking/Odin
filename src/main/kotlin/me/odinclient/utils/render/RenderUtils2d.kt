package me.odinclient.utils.render

import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import java.awt.Color
import java.util.*

object RenderUtils2d {

    fun drawRect(x: Float, y: Float, width: Float, height: Float, color: Color) {
        val pos = mutableListOf(x, y, x + width, y + height)
        if (pos[0] > pos[2])
            Collections.swap(pos, 0, 2)
        if (pos[1] > pos[3])
            Collections.swap(pos, 1, 3)

        GlStateManager.pushMatrix()
        GlStateManager.enableBlend()
        GlStateManager.disableTexture2D()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)

        GlStateManager.color(color.red / 255f, color.green / 255f, color.blue / 255f, color.alpha / 255f)

        val worldRenderer = Tessellator.getInstance().worldRenderer

        worldRenderer.begin(7, DefaultVertexFormats.POSITION)
        worldRenderer.pos(pos[0].toDouble(), pos[3].toDouble(), 0.0).endVertex()
        worldRenderer.pos(pos[2].toDouble(), pos[3].toDouble(), 0.0).endVertex()
        worldRenderer.pos(pos[2].toDouble(), pos[1].toDouble(), 0.0).endVertex()
        worldRenderer.pos(pos[0].toDouble(), pos[1].toDouble(), 0.0).endVertex()

        Tessellator.getInstance().draw()

        GlStateManager.color(1f, 1f, 1f, 1f)
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()

        GlStateManager.popMatrix()
    }
}