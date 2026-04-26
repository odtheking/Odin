package com.odtheking.odin.utils.render

import com.mojang.blaze3d.buffers.Std140Builder
import com.mojang.blaze3d.buffers.Std140SizeCalculator
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.DefaultVertexFormat
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.Tesselator
import com.mojang.blaze3d.vertex.VertexFormat
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.navigation.ScreenRectangle
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer
import net.minecraft.client.renderer.DynamicUniformStorage
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState
import org.joml.*
import java.util.*
import kotlin.math.roundToInt

class RoundRectPIPRenderer(bufferSource: MultiBufferSource.BufferSource)
    : PictureInPictureRenderer<RoundRectPIPRenderer.State>(bufferSource) {

    private var lastState: State? = null

    override fun getRenderStateClass(): Class<State> = State::class.java

    override fun textureIsReadyToBlit(state: State): Boolean = state.visuallyEquals(lastState)

    override fun renderToTexture(state: State, poseStack: PoseStack) {
        val w = state.width * state.scale
        val h = state.height * state.scale

        val builder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR)
        builder.addVertex(0f, 0f, 0f).setColor(state.topLeftColor)
        builder.addVertex(0f, h, 0f).setColor(state.bottomLeftColor)
        builder.addVertex(w, h, 0f).setColor(state.bottomRightColor)
        builder.addVertex(w, 0f, 0f).setColor(state.topRightColor)
        val mesh = builder.buildOrThrow()

        val dynamicTransforms = RenderSystem.getDynamicUniforms().writeTransform(
            RenderSystem.getModelViewMatrix(), Vector4f(1f, 1f, 1f, 1f), Vector3f(), Matrix4f()
        )

        val uniformBuffer = uniformStorage.writeUniform { buffer ->
            Std140Builder.intoBuffer(buffer)
                .putVec4(w * 0.5f, h * 0.5f, w, h)                                          // u_Rect
                .putVec4(state.topLeftRadius * state.scale, state.topRightRadius * state.scale, state.bottomRightRadius * state.scale, state.bottomLeftRadius * state.scale) // u_Radii
                .putVec4(state.outlineRed, state.outlineGreen, state.outlineBlue, state.outlineAlpha) // u_OutlineColor
                .putVec4(state.outlineWidth * state.scale, 0f, 0f, 0f)                       // u_OutlineWidth (std140 padded)
        }

        val vertexBuffer = CustomRenderPipelines.PIPELINE_ROUND_RECT.vertexFormat.uploadImmediateVertexBuffer(mesh.vertexBuffer())
        val indexStorage = RenderSystem.getSequentialBuffer(mesh.drawState().mode())
        val indexBuffer = indexStorage.getBuffer(mesh.drawState().indexCount())
        val renderTarget = Minecraft.getInstance().mainRenderTarget

        mesh.use {
            (RenderSystem.outputColorTextureOverride ?: renderTarget.colorTextureView)?.let { gpuTextureView ->
                RenderSystem.getDevice().createCommandEncoder().createRenderPass(
                    { "Odin Rounded Rectangle" }, gpuTextureView, OptionalInt.empty(),
                    if (renderTarget.useDepth) Objects.requireNonNullElse(RenderSystem.outputDepthTextureOverride, renderTarget.depthTextureView) else null,
                    OptionalDouble.empty()
                )
            }?.use { pass ->
                pass.setPipeline(CustomRenderPipelines.PIPELINE_ROUND_RECT)
                RenderSystem.bindDefaultUniforms(pass)
                pass.setUniform("DynamicTransforms", dynamicTransforms)
                pass.setUniform("u", uniformBuffer)
                pass.setVertexBuffer(0, vertexBuffer)
                pass.setIndexBuffer(indexBuffer, indexStorage.type())
                pass.drawIndexed(0, 0, mesh.drawState().indexCount(), 1)
            }
        }

        lastState = state
    }

    override fun getTextureLabel(): String = "Odin Rounded Rectangle PIP"

    class State(
        val x: Int,
        val y: Int,
        val width: Int,
        val height: Int,
        val topLeftColor: Int,
        val topRightColor: Int,
        val bottomRightColor: Int,
        val bottomLeftColor: Int,
        val topLeftRadius: Float,
        val topRightRadius: Float,
        val bottomRightRadius: Float,
        val bottomLeftRadius: Float,
        val outlineColor: Int,
        val outlineWidth: Float,
        private val scissorArea: ScreenRectangle?,
        private val bounds: ScreenRectangle?
    ) : PictureInPictureRenderState {

        val scale = Minecraft.getInstance().window.guiScale.toFloat()

        val outlineRed   = (outlineColor shr 16 and 0xFF) / 255f
        val outlineGreen = (outlineColor shr 8  and 0xFF) / 255f
        val outlineBlue  = (outlineColor        and 0xFF) / 255f
        val outlineAlpha = (outlineColor shr 24 and 0xFF) / 255f

        override fun x0() = x
        override fun y0() = y
        override fun x1() = x + width
        override fun y1() = y + height
        override fun scale() = 1f
        override fun scissorArea() = scissorArea
        override fun bounds() = bounds

        fun visuallyEquals(other: State?): Boolean {
            if (other == null) return false
            return width == other.width &&
                height == other.height &&
                topLeftColor == other.topLeftColor &&
                topRightColor == other.topRightColor &&
                bottomRightColor == other.bottomRightColor &&
                bottomLeftColor == other.bottomLeftColor &&
                topLeftRadius == other.topLeftRadius &&
                topRightRadius == other.topRightRadius &&
                bottomRightRadius == other.bottomRightRadius &&
                bottomLeftRadius == other.bottomLeftRadius &&
                outlineColor == other.outlineColor &&
                outlineWidth == other.outlineWidth &&
                scale == other.scale
        }
    }

    companion object {
        private val uniformStorage = DynamicUniformStorage<DynamicUniformStorage.DynamicUniform>(
            "Odin Rounded Rectangle UBO",
            Std140SizeCalculator()
                .putVec4() // u_Rect
                .putVec4() // u_Radii
                .putVec4() // u_OutlineColor
                .putVec4() // u_OutlineWidth (std140 padded)
                .get(),
            4
        )

        fun clear() = uniformStorage.endFrame()

        fun submit(
            context: GuiGraphicsExtractor,
            x0: Int, y0: Int, x1: Int, y1: Int,
            topLeftColor: Int, topRightColor: Int, bottomRightColor: Int, bottomLeftColor: Int,
            topLeftRadius: Float, topRightRadius: Float, bottomRightRadius: Float, bottomLeftRadius: Float,
            outlineColor: Int, outlineWidth: Float
        ) {
            val scissor = context.scissorStack.peek()
            val pose = Matrix3x2f(context.pose())

            val p0 = pose.transformPosition(Vector2f(x0.toFloat(), y0.toFloat()))
            val p1 = pose.transformPosition(Vector2f(x1.toFloat(), y1.toFloat()))

            val screenLeft  = minOf(p0.x, p1.x).roundToInt()
            val screenTop   = minOf(p0.y, p1.y).roundToInt()
            val screenW     = maxOf(p0.x, p1.x).roundToInt() - screenLeft
            val screenH     = maxOf(p0.y, p1.y).roundToInt() - screenTop

            val poseScale   = pose.transformDirection(Vector2f(1f, 0f)).length()

            val screenRect = ScreenRectangle(screenLeft, screenTop, screenW, screenH)
            val bounds = if (scissor != null) scissor.intersection(screenRect) else screenRect

            context.guiRenderState.addPicturesInPictureState(
                State(
                    screenLeft, screenTop, screenW, screenH,
                    topLeftColor, topRightColor, bottomRightColor, bottomLeftColor,
                    topLeftRadius * poseScale, topRightRadius * poseScale,
                    bottomRightRadius * poseScale, bottomLeftRadius * poseScale,
                    outlineColor, outlineWidth * poseScale,
                    scissor, bounds
                )
            )
        }
    }
}
