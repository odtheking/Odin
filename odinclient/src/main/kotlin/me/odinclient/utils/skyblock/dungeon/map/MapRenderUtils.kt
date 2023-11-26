package me.odinclient.utils.skyblock.dungeon.map

import me.odinclient.dungeonmap.core.DungeonPlayer
import me.odinclient.dungeonmap.features.DungeonScan
import me.odinclient.features.impl.dungeon.MapModule
import me.odinclient.utils.skyblock.dungeon.map.MapUtils.coordMultiplier
import me.odinmain.OdinMain.mc
import me.odinmain.utils.equalsOneOf
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.world.RenderUtils.bindColor
import me.odinmain.utils.skyblock.itemID
import net.minecraft.client.gui.Gui
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.WorldRenderer
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import org.lwjgl.opengl.GL11
import java.awt.image.BufferedImage
import java.nio.ByteBuffer


object MapRenderUtils {

    private val tessellator: Tessellator = Tessellator.getInstance()
    private val worldRenderer: WorldRenderer = tessellator.worldRenderer

    fun renderRect(x: Double, y: Double, w: Double, h: Double, color: Color) {
        if (color.isTransparent) return
        GlStateManager.enableBlend()
        GlStateManager.disableTexture2D()
        GlStateManager.enableAlpha()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        color.bindColor()

        worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION)
        addQuadVertices(x, y, w, h)
        tessellator.draw()

        GlStateManager.disableAlpha()
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
    }

    fun renderRectBorder(x: Double, y: Double, w: Double, h: Double, thickness: Double, color: Color) {
        if (color.isTransparent) return
        GlStateManager.enableBlend()
        GlStateManager.disableTexture2D()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        color.bindColor()

        worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION)
        GlStateManager.shadeModel(GL11.GL_FLAT)

        addQuadVertices(x - thickness, y, thickness, h)
        addQuadVertices(x - thickness, y - thickness, w + thickness * 2, thickness)
        addQuadVertices(x + w, y, thickness, h)
        addQuadVertices(x - thickness, y + h, w + thickness * 2, thickness)

        tessellator.draw()

        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
        GlStateManager.shadeModel(GL11.GL_SMOOTH)
    }

    private fun addQuadVertices(x: Double, y: Double, w: Double, h: Double) {
        worldRenderer.pos(x, y + h, 0.0).endVertex()
        worldRenderer.pos(x + w, y + h, 0.0).endVertex()
        worldRenderer.pos(x + w, y, 0.0).endVertex()
        worldRenderer.pos(x, y, 0.0).endVertex()
    }

    fun renderCenteredText(text: List<String>, x: Int, y: Int, color: Int) {
        GlStateManager.pushMatrix()

        GlStateManager.translate(x.toFloat(), y.toFloat(), 0f)
        GlStateManager.scale(MapModule.textScale, MapModule.textScale, 1f)

        if (text.isNotEmpty()) {
            val yTextOffset = text.size * 5f
            for (i in text.indices) {
                mc.fontRendererObj.drawString(
                    text[i],
                    (-mc.fontRendererObj.getStringWidth(text[i]) shr 1).toFloat(),
                    i * 10 - yTextOffset,
                    color,
                    true
                )
            }
        }
        GlStateManager.popMatrix()
    }

    fun drawTexturedModalRect(x: Int, y: Int, width: Int, height: Int) {
        worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX)
        worldRenderer.pos(x.toDouble(), (y + height).toDouble(), 0.0).tex(0.0, 1.0).endVertex()
        worldRenderer.pos((x + width).toDouble(), (y + height).toDouble(), 0.0).tex(1.0, 1.0).endVertex()
        worldRenderer.pos((x + width).toDouble(), y.toDouble(), 0.0).tex(1.0, 0.0).endVertex()
        worldRenderer.pos(x.toDouble(), y.toDouble(), 0.0).tex(0.0, 0.0).endVertex()
        tessellator.draw()
    }

    fun drawPlayerHead(name: String, player: DungeonPlayer) {
        GlStateManager.pushMatrix()
        try {
            if (name == mc.thePlayer.name) {
                GlStateManager.translate(
                    (mc.thePlayer.posX - DungeonScan.startX + 15) * coordMultiplier + MapUtils.startCorner.first - 2,
                    (mc.thePlayer.posZ - DungeonScan.startZ + 15) * coordMultiplier + MapUtils.startCorner.second - 2,
                    0.0
                )
            } else {
                GlStateManager.translate(player.mapX.toFloat(), player.mapZ.toFloat(), 0f)
            }

            if (MapModule.playerHeads == 2 || MapModule.playerHeads == 1 && mc.thePlayer.heldItem?.itemID.equalsOneOf(
                    "SPIRIT_LEAP",
                    "INFINITE_SPIRIT_LEAP"
                )
            ) {
                GlStateManager.pushMatrix()
                GlStateManager.scale(0.8, 0.8, 1.0)
                mc.fontRendererObj.drawString(
                    name,
                    -mc.fontRendererObj.getStringWidth(name) shr 1,
                    10,
                    0xffffff
                )
                GlStateManager.popMatrix()
            }

            GlStateManager.rotate(player.yaw + 180f, 0f, 0f, 1f)
            GlStateManager.scale(MapModule.playerHeadScale, MapModule.playerHeadScale, 1f)

            renderRectBorder(-6.0, -6.0, 12.0, 12.0, 1.0, Color.BLACK)
            GlStateManager.color(1f, 1f, 1f, 1f)
            mc.textureManager.bindTexture(player.skin)

            Gui.drawScaledCustomSizeModalRect(-6, -6, 8f, 8f, 8, 8, 12, 12, 64f, 64f)
            if (player.renderHat) {
                Gui.drawScaledCustomSizeModalRect(-6, -6, 40f, 8f, 8, 8, 12, 12, 64f, 64f)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        GlStateManager.popMatrix()
    }

    fun createBufferedImageFromTexture(textureID: Int): BufferedImage {
        GlStateManager.bindTexture(textureID)

        // Retrieve the width and height of the texture
        val width = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_WIDTH)
        val height = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_HEIGHT)

        // Create a ByteBuffer to hold the pixel data
        val pixelBytes = 4 // Assuming 32-bit RGBA texture
        val buffer: ByteBuffer = ByteBuffer.allocateDirect(width * height * pixelBytes)


        // Retrieve the pixel data from the texture
        GL11.glGetTexImage(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer)

        val pixelData = ByteArray(width * height * pixelBytes)
        buffer.get(pixelData)
        val image = BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR)
        val raster = image.raster
        raster.setDataElements(0, 0, width, height, pixelData)

        // Release the texture by unbinding it
        GlStateManager.bindTexture(0)

        return image
    }
}