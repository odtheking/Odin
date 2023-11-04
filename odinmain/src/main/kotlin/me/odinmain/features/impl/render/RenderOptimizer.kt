package me.odinmain.features.impl.render

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.item.EntityFallingBlock
import net.minecraftforge.event.entity.EntityJoinWorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object RenderOptimizer : Module(
    name = "Render Optimizer",
    category = Category.RENDER,
    description = "Disables certain render function when they are not necessary, resulting in a decrease in gpu usage."
) {

    //private val decreaseGpuUsage: Boolean by BooleanSetting(name = "Reduce GPU Usage", default = true)
    private val fallingBlocks: Boolean by BooleanSetting(name = "Remove falling blocks", default = true)
    private val p5Mobs: Boolean by BooleanSetting(name = "Remove P5 Armor Stands", default = true)

    @SubscribeEvent
    fun onFallingBlock(event: EntityJoinWorldEvent) {
        if (
            event.entity is EntityArmorStand &&
            p5Mobs &&
            DungeonUtils.getPhase() == 5 &&
            event.entity.posY < 15 && // don't kill dragon tags
            event.entity.posX !in 47.0..61.0 && event.entity.posZ !in 70.0..84.0 // chest positions
        )
            event.entity.setDead()

        if (event.entity is EntityFallingBlock && fallingBlocks) event.entity.setDead()
    }

    /*fun drawGui() {
        if (!decreaseGpuUsage) return
        if (mc.skipRenderWorld && mc.currentScreen != null) {
            mc.setIngameNotInFocus()
            if (mc.theWorld == null) {
                GlStateManager.viewport(0, 0, mc.displayWidth, mc.displayHeight)
                GlStateManager.matrixMode(5889)
                GlStateManager.loadIdentity()
                GlStateManager.matrixMode(5888)
                GlStateManager.loadIdentity()
                mc.entityRenderer.setupOverlayRendering()
            } else {
                GlStateManager.alphaFunc(516, 0.1f)
                mc.entityRenderer.setupOverlayRendering()
            }
            val scaledResolution = ScaledResolution(mc)
            val i1: Int = scaledResolution.scaledWidth
            val j1: Int = scaledResolution.scaledHeight
            val k1: Int = Mouse.getX() * i1 / mc.displayWidth
            val l1: Int = j1 - Mouse.getY() * j1 / mc.displayHeight - 1
            GlStateManager.clear(256)
            drawRect(i1.toFloat(), j1.toFloat(), Color.black.rgb)
            try {
                ForgeHooksClient.drawScreen(mc.currentScreen, k1, l1, RenderUtils.partialTicks)
            } catch (throwable: Throwable) {
                throwable.printStackTrace()
            }
        }
    }

    @SubscribeEvent
    fun onPostGui(event: PostGuiOpenEvent) {
        if (!decreaseGpuUsage) return
        mc.skipRenderWorld = true
    }

    private fun drawRect(right: Float, bottom: Float, color: Int) {
        if (!decreaseGpuUsage) return
        var left = 0f
        var top = 0f
        var right = right
        var bottom = bottom
        if (left < right) {
            val i = left
            left = right
            right = i
        }
        if (top < bottom) {
            val j = top
            top = bottom
            bottom = j
        }
        val f3 = (color shr 24 and 0xFF) / 255.0f
        val f4 = (color shr 16 and 0xFF) / 255.0f
        val f5 = (color shr 8 and 0xFF) / 255.0f
        val f6 = (color and 0xFF) / 255.0f

        val tessellator: Tessellator = Tessellator.getInstance()
        val worldRenderer: WorldRenderer = tessellator.worldRenderer
        GlStateManager.enableBlend()
        GlStateManager.disableTexture2D()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        GlStateManager.color(f4, f5, f6, f3)
        worldRenderer.begin(7, DefaultVertexFormats.POSITION)
        worldRenderer.pos(left.toDouble(), bottom.toDouble(), 0.0).endVertex()
        worldRenderer.pos(right.toDouble(), bottom.toDouble(), 0.0).endVertex()
        worldRenderer.pos(right.toDouble(), top.toDouble(), 0.0).endVertex()
        worldRenderer.pos(left.toDouble(), top.toDouble(), 0.0).endVertex()
        tessellator.draw()
        GlStateManager.color(1.0f, 1.0f, 1.0f)
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
    }*/
}