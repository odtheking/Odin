package me.odinclient.features.impl.general

import me.odinclient.OdinClient.Companion.mc
import me.odinclient.events.impl.PostGuiOpenEvent
import me.odinclient.features.Category
import me.odinclient.features.Module
import me.odinclient.features.settings.impl.BooleanSetting
import me.odinclient.mixin.MinecraftAccessor
import me.odinclient.ui.clickgui.ClickGUI
import me.odinclient.utils.render.gui.animations.impl.EaseInOut
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.WorldRenderer
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.entity.item.EntityFallingBlock
import net.minecraftforge.client.ForgeHooksClient
import net.minecraftforge.event.entity.EntityJoinWorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.input.Mouse
import java.awt.Color

// TODO: I think this needs a better name
object NoRender : Module(
    name = "No Render",
    category = Category.GENERAL,
    description = "Disables certain render function when they are not necessary, resulting in a decrease in gpu usage"
) {

    private val fallingBlocks: Boolean by BooleanSetting("Remove falling blocks", true)

    @SubscribeEvent
    fun onFallingBlock(event: EntityJoinWorldEvent) {
        if (event.entity !is EntityFallingBlock || !this.fallingBlocks) return
        event.entity.setDead()
    }

    override fun onEnable()
    {
        mc.skipRenderWorld = true
        ClickGUI.anim = EaseInOut(0)
        mc.displayGuiScreen(ClickGUI)
        ClickGUI.anim = EaseInOut(200)
    }

    override fun onDisable()
    {
        mc.skipRenderWorld = false
    }

    fun drawGui() {
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
            this.drawRect(i1.toFloat(), j1.toFloat(), Color.black.rgb)
            try {
                ForgeHooksClient.drawScreen(mc.currentScreen, k1, l1, (mc as MinecraftAccessor).timer.renderPartialTicks)
            } catch (throwable: Throwable) {
                throwable.printStackTrace()
            }
        }
    }

    @SubscribeEvent
    fun onPostGui(event: PostGuiOpenEvent)
    {
        mc.skipRenderWorld = true
    }

    private fun drawRect(right: Float, bottom: Float, color: Int)
    {
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
    }
}