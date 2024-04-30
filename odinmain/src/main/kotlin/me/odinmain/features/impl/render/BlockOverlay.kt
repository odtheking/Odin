package me.odinmain.features.impl.render

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.ActionSetting
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.ColorSetting
import me.odinmain.features.settings.impl.NumberSetting
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.RenderUtils.bind
import me.odinmain.utils.toAABB
import net.minecraft.block.Block
import net.minecraft.block.material.Material
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderGlobal
import net.minecraft.util.BlockPos
import net.minecraft.util.MovingObjectPosition.MovingObjectType
import net.minecraftforge.client.event.DrawBlockHighlightEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.opengl.GL11

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
        if (event.target.typeOfHit != MovingObjectType.BLOCK) return
        event.isCanceled = true

        color.bind()
        GL11.glLineWidth(lineWidth)
        GlStateManager.disableTexture2D()
        GlStateManager.enableBlend()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        if (lineSmoothing) {
            GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST)
            GL11.glEnable(GL11.GL_LINE_SMOOTH)
        } else {
            GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_DONT_CARE)
            GL11.glDisable(GL11.GL_LINE_SMOOTH)
        }
        if (disableDepth) {
            GlStateManager.depthMask(false)
            GlStateManager.disableDepth()
        }

        val blockPos: BlockPos = event.target.blockPos
        val block: Block = mc.theWorld.getBlockState(blockPos).block
        if (block.material !== Material.air && mc.theWorld.worldBorder.contains(blockPos)) {
            block.setBlockBoundsBasedOnState(mc.theWorld, blockPos)
            val d0: Double = event.player.lastTickPosX + (event.player.posX - event.player.lastTickPosX) * event.partialTicks
            val d1: Double = event.player.lastTickPosY + (event.player.posY - event.player.lastTickPosY) * event.partialTicks
            val d2: Double = event.player.lastTickPosZ + (event.player.posZ - event.player.lastTickPosZ) * event.partialTicks
            val aabb = if (fullBlock) blockPos.toAABB().expand(-0.008 + expand / 1000.0, -0.008 + expand / 1000.0, -0.008 + expand / 1000.0).offset(-d0, -d1, -d2) else block.getSelectedBoundingBox(mc.theWorld, blockPos).expand(0.002 + expand / 1000f, 0.002 + expand / 1000f, 0.002 + expand / 1000f).offset(-d0, -d1, -d2)
            RenderGlobal.drawSelectionBoundingBox(aabb)
        }

        if (disableDepth) {
            GlStateManager.depthMask(true)
            GlStateManager.enableDepth()
        }
        GlStateManager.disableBlend()
        GlStateManager.enableTexture2D()
    }

}