package com.odtheking.odin.utils.render

import com.mojang.blaze3d.pipeline.BlendFunction
import com.mojang.blaze3d.pipeline.RenderPipeline
import com.mojang.blaze3d.platform.DepthTestFunction
import com.mojang.blaze3d.shaders.UniformType
import com.mojang.blaze3d.vertex.DefaultVertexFormat
import com.mojang.blaze3d.vertex.VertexFormat
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.resources.ResourceLocation

object CustomRenderPipelines {

    val LINE_LIST: RenderPipeline = RenderPipelines.register(
        RenderPipeline.builder(*arrayOf<RenderPipeline.Snippet?>(RenderPipelines.LINES_SNIPPET))
            .withLocation("pipeline/lines")
            .withVertexFormat(DefaultVertexFormat.POSITION_COLOR_NORMAL, VertexFormat.Mode.LINES)
            .withCull(false)
            .withBlend(BlendFunction.TRANSLUCENT)
            .withDepthWrite(true)
            .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
            .build()
    )

    val LINE_LIST_ESP: RenderPipeline = RenderPipelines.register(
        RenderPipeline.builder(*arrayOf<RenderPipeline.Snippet?>(RenderPipelines.LINES_SNIPPET))
            .withLocation("pipeline/lines")
            .withShaderDefine("shad")
            .withVertexFormat(DefaultVertexFormat.POSITION_COLOR_NORMAL, VertexFormat.Mode.LINES)
            .withCull(false)
            .withBlend(BlendFunction.TRANSLUCENT)
            .withDepthWrite(false)
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .build()
    )

    val TRIANGLE_STRIP: RenderPipeline = RenderPipelines.register(
        RenderPipeline.builder(*arrayOf<RenderPipeline.Snippet?>(RenderPipelines.DEBUG_FILLED_SNIPPET))
            .withLocation("pipeline/debug_filled_box")
            .withCull(false)
            .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.TRIANGLE_STRIP)
            .withDepthWrite(true)
            .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
            .withBlend(BlendFunction.TRANSLUCENT)
            .build()
    )

    val TRIANGLE_STRIP_ESP: RenderPipeline = RenderPipelines.register(
        RenderPipeline.builder(*arrayOf<RenderPipeline.Snippet?>(RenderPipelines.DEBUG_FILLED_SNIPPET))
            .withLocation("pipeline/debug_filled_box")
            .withCull(false)
            .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.TRIANGLE_STRIP)
            .withDepthWrite(false)
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withBlend(BlendFunction.TRANSLUCENT)
            .build()
    )

    val PIPELINE_ROUND_RECT: RenderPipeline = RenderPipelines.register(
        RenderPipeline.builder(*arrayOf<RenderPipeline.Snippet?>(RenderPipelines.GUI_SNIPPET))
            .withLocation(ResourceLocation.fromNamespaceAndPath("odin", "pipeline/round_rect"))
            .withFragmentShader(ResourceLocation.fromNamespaceAndPath("odin", "core/round_rect"))
            .withVertexShader(ResourceLocation.fromNamespaceAndPath("odin", "core/round_rect"))
            .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS)
            .withUniform("u", UniformType.UNIFORM_BUFFER)
            .withBlend(BlendFunction.TRANSLUCENT)
            .build()
    )
}