package com.odtheking.odin.utils.render

import com.mojang.blaze3d.platform.Lighting
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.textures.FilterMode
import com.mojang.blaze3d.textures.GpuTextureView
import com.mojang.blaze3d.vertex.PoseStack
import com.odtheking.odin.OdinMod.mc
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.navigation.ScreenRectangle
import net.minecraft.client.gui.render.TextureSetup
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.client.renderer.item.TrackingItemStackRenderState
import net.minecraft.client.renderer.state.gui.BlitRenderState
import net.minecraft.client.renderer.state.gui.GuiItemRenderState
import net.minecraft.client.renderer.state.gui.GuiRenderState
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.util.LightCoordsUtil
import net.minecraft.world.item.ItemDisplayContext
import net.minecraft.world.item.ItemStack
import org.joml.Matrix3x2f
import java.util.*

class ItemStateRenderer(vertexConsumers: MultiBufferSource.BufferSource)
    : PictureInPictureRenderer<ItemStateRenderer.State>(vertexConsumers) {

    private var textureView: GpuTextureView? = null
    private var lastState: State? = null

    override fun renderToTexture(renderState: State, poseStack: PoseStack) {
        textureView = RenderSystem.outputColorTextureOverride
        lastState = renderState
        poseStack.scale(1f, -1f, -1f)

        if (renderState.state.itemStackRenderState().usesBlockLight()) mc.gameRenderer.lighting.setupFor(Lighting.Entry.ITEMS_3D)
        else mc.gameRenderer.lighting.setupFor(Lighting.Entry.ITEMS_FLAT)

        val dispatcher = mc.gameRenderer.featureRenderDispatcher
        renderState.state.itemStackRenderState().submit(poseStack, dispatcher.submitNodeStorage, LightCoordsUtil.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 0)
        dispatcher.renderAllFeatures()
    }

    override fun blitTexture(renderState: State, state: GuiRenderState) {
        state.addBlitToCurrentLayer(
            BlitRenderState(
                RenderPipelines.GUI_TEXTURED_PREMULTIPLIED_ALPHA, TextureSetup.singleTexture(textureView!!, RenderSystem.getSamplerCache().getRepeat(FilterMode.LINEAR)),
                renderState.pose(), renderState.x0(), renderState.y0(), renderState.x0() + 16, renderState.y0() + 16,
                0.0f, 1.0f, 1.0f, 0.0f, -1, renderState.scissorArea(), null
            )
        )
    }

    override fun textureIsReadyToBlit(state: State): Boolean = lastState != null && lastState == state
    override fun getTranslateY(height: Int, windowScaleFactor: Int): Float = height / 2f
    override fun getRenderStateClass(): Class<State> = State::class.java
    override fun getTextureLabel(): String = "item_state"

    data class State(val state: GuiItemRenderState) : PictureInPictureRenderState {
        override fun scale(): Float = maxOf(state.pose().m00(), state.pose().m11()) * 16f
        override fun x0(): Int = state.x()
        override fun y0(): Int = state.y()
        override fun x1(): Int = state.x() + scale().toInt()
        override fun y1(): Int = state.y() + scale().toInt()
        override fun scissorArea(): ScreenRectangle? = state.scissorArea()
        override fun bounds(): ScreenRectangle? = state.bounds()
        override fun pose(): Matrix3x2f = state.pose()

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is State) return false
            if (other.state.itemStackRenderState().modelIdentity != state.itemStackRenderState().modelIdentity) return false
            if (other.state.pose().m00() != state.pose().m00()) return false
            if (other.state.pose().m11() != state.pose().m11()) return false
            return true
        }

        override fun hashCode(): Int {
            return Objects.hash(state.itemStackRenderState().modelIdentity, state.pose().m00(), state.pose().m11())
        }
    }

    companion object {
        fun GuiGraphicsExtractor.drawItemStack(item: ItemStack, x: Int, y: Int) {
            if (item.isEmpty) return

            val tracking = TrackingItemStackRenderState()
            mc.itemModelResolver.updateForTopItem(tracking, item, ItemDisplayContext.GUI, mc.level, mc.player, 0)

            val state = State(
                GuiItemRenderState(
                    Matrix3x2f(pose()),
                    tracking,
                    x, y,
                    scissorStack.peek()
                )
            )
            guiRenderState.addPicturesInPictureState(state)
        }
    }
}
