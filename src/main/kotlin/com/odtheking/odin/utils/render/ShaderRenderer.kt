package com.odtheking.odin.utils.render

import com.mojang.blaze3d.ProjectionType
import com.mojang.blaze3d.buffers.Std140Builder
import com.mojang.blaze3d.buffers.Std140SizeCalculator
import com.mojang.blaze3d.pipeline.RenderTarget
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.DefaultVertexFormat
import com.mojang.blaze3d.vertex.Tesselator
import com.mojang.blaze3d.vertex.VertexFormat
import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.utils.Color.Companion.alpha
import com.odtheking.odin.utils.Color.Companion.blue
import com.odtheking.odin.utils.Color.Companion.green
import com.odtheking.odin.utils.Color.Companion.red
import net.minecraft.client.renderer.CachedOrthoProjectionMatrixBuffer
import net.minecraft.client.renderer.DynamicUniformStorage
import org.joml.Matrix3x2f
import org.joml.Matrix4f
import org.joml.Vector3f
import org.joml.Vector4f
import java.util.*

internal object ShaderRenderer {

    private val uniformStorage = DynamicUniformStorage<DynamicUniformStorage.DynamicUniform>(
        "Odin Rounded Rectangle UBO",
        Std140SizeCalculator()
            .putVec4() // u_Rect
            .putVec4() // u_Radii
            .putVec4() // u_OutlineColor
            .putVec4() // u_OutlineWidth (std140 padded slot)
            .get(),
        4
    )

    private val colorModulator = Vector4f(1.0f, 1.0f, 1.0f, 1.0f)
    private val lightDirection = Vector3f()

    val projectionMatrix = CachedOrthoProjectionMatrixBuffer("Odin Rounded", -1000f, 1000f, true)

    fun renderRoundedRect(
        pose: Matrix3x2f, x0: Int, y0: Int, x1: Int, y1: Int,
        topLeftColor: Int, topRightColor: Int, bottomRightColor: Int, bottomLeftColor: Int,
        topLeftRadius: Float, topRightRadius: Float, bottomRightRadius: Float, bottomLeftRadius: Float,
        outlineColor: Int, outlineWidth: Float, renderTarget: RenderTarget
    ) {
        val colorTextureView = RenderSystem.outputColorTextureOverride ?: renderTarget.colorTextureView ?: return

        val x0f = x0.toFloat()
        val y0f = y0.toFloat()
        val x1f = x1.toFloat()
        val y1f = y1.toFloat()

        val builder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR)
        builder.addVertex(x0f, y0f, 0f).setColor(topLeftColor)
        builder.addVertex(x0f, y1f, 0f).setColor(bottomLeftColor)
        builder.addVertex(x1f, y1f, 0f).setColor(bottomRightColor)
        builder.addVertex(x1f, y0f, 0f).setColor(topRightColor)
        val mesh = builder.buildOrThrow()

        val window = mc.window
        RenderSystem.backupProjectionMatrix()
        RenderSystem.setProjectionMatrix(
            projectionMatrix.getBuffer(
                window.width.toFloat() / window.guiScale.toFloat(),
                window.height.toFloat() / window.guiScale.toFloat()
            ),
            ProjectionType.ORTHOGRAPHIC
        )

        val modelMatrix = Matrix4f(
            pose.m00(), pose.m01(), 0f, 0f,
            pose.m10(), pose.m11(), 0f, 0f,
            0f, 0f, 1f, 0f,
            pose.m20(), pose.m21(), 1000f, 1f
        )

        val dynamicTransforms = RenderSystem.getDynamicUniforms().writeTransform(
            modelMatrix, colorModulator, lightDirection, Matrix4f()
        )

        val uniforms = uniformStorage.writeUniform { buffer ->
            Std140Builder.intoBuffer(buffer)
                .putVec4((x0f + x1f) * 0.5f, (y0f + y1f) * 0.5f, x1f - x0f, y1f - y0f)
                .putVec4(topLeftRadius, topRightRadius, bottomRightRadius, bottomLeftRadius)
                .putVec4(outlineColor.red / 255.0f, outlineColor.green / 255.0f, outlineColor.blue / 255.0f, outlineColor.alpha / 255.0f)
                .putVec4(outlineWidth, 0.0f, 0.0f, 0.0f)
        }

        val vertexBuffer = CustomRenderPipelines.PIPELINE_ROUND_RECT.vertexFormat.uploadImmediateVertexBuffer(mesh.vertexBuffer())
        val indexStorage = RenderSystem.getSequentialBuffer(mesh.drawState().mode())
        val indexBuffer = indexStorage.getBuffer(mesh.drawState().indexCount())

        mesh.use {
            val pass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(
                { "Odin Rounded Rectangle" },
                colorTextureView,
                OptionalInt.empty(),
                if (renderTarget.useDepth) RenderSystem.outputDepthTextureOverride ?: renderTarget.depthTextureView else null,
                OptionalDouble.empty()
            )

            pass.use {
                it.setPipeline(CustomRenderPipelines.PIPELINE_ROUND_RECT)
                RenderSystem.bindDefaultUniforms(it)
                it.setUniform("DynamicTransforms", dynamicTransforms)
                it.setUniform("u", uniforms)
                it.setVertexBuffer(0, vertexBuffer)
                it.setIndexBuffer(indexBuffer, indexStorage.type())
                it.drawIndexed(0, 0, mesh.drawState().indexCount(), 1)
            }
        }
        RenderSystem.restoreProjectionMatrix()
    }

    fun clear() {
        uniformStorage.endFrame()
    }
}