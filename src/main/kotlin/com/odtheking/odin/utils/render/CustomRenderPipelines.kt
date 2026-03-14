package com.odtheking.odin.utils.render

import com.mojang.blaze3d.pipeline.BlendFunction
import com.mojang.blaze3d.pipeline.RenderPipeline
import com.mojang.blaze3d.platform.DepthTestFunction
import com.mojang.blaze3d.shaders.UniformType
import com.mojang.blaze3d.vertex.DefaultVertexFormat
import com.mojang.blaze3d.vertex.VertexFormat
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.resources.Identifier

object CustomRenderPipelines {
    val LINES_ESP: RenderPipeline = RenderPipelines.register(
        RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withLocation("odin/lines_esp")
            .build()
    )

    val LINES_TRANSLUCENT_ESP: RenderPipeline = RenderPipelines.register(
        RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withLocation("odin/lines_translucent_esp")
            .withDepthWrite(false)
            .build()
    )

    val QUADS_ESP: RenderPipeline = RenderPipelines.register(
        RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withLocation("odin/quads_esp")
            .build()
    )

    val PIPELINE_ROUND_RECT: RenderPipeline = RenderPipelines.register(
        RenderPipeline.builder(RenderPipelines.GUI_SNIPPET)
            .withLocation(Identifier.fromNamespaceAndPath("odin", "pipeline/round_rect"))
            .withFragmentShader(Identifier.fromNamespaceAndPath("odin", "core/round_rect"))
            .withVertexShader(Identifier.fromNamespaceAndPath("odin", "core/round_rect"))
            .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS)
            .withUniform("u", UniformType.UNIFORM_BUFFER)
            .withBlend(BlendFunction.TRANSLUCENT)
            .build()
    )
}