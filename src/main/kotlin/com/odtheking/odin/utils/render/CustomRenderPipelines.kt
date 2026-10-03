package com.odtheking.odin.utils.render

import com.mojang.blaze3d.vertex.DefaultVertexFormat
import com.mojang.renderpearl.api.pipeline.*
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.resources.Identifier

object CustomRenderPipelines {
    private val NO_DEPTH = DepthStencilState(CompareOp.ALWAYS_PASS, false)
    private val TRANSLUCENT = ColorTargetState(BlendFunction.TRANSLUCENT)

    val LINES_ESP: RenderPipeline = RenderPipelines.register(
        RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)
            .withDepthStencilState(NO_DEPTH)
            .withColorTargetState(ColorTargetState.DEFAULT)
            .withLocation("odin/lines_esp")
            .build()
    )

    val LINES_TRANSLUCENT_ESP: RenderPipeline = RenderPipelines.register(
        RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)
            .withDepthStencilState(NO_DEPTH)
            .withColorTargetState(TRANSLUCENT)
            .withLocation("odin/lines_translucent_esp")
            .build()
    )

    val QUADS_OPAQUE: RenderPipeline = RenderPipelines.register(
        RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
            .withCull(false)
            .withLocation("odin/quads_opaque")
            .build()
    )

    val QUADS_ESP: RenderPipeline = RenderPipelines.register(
        RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
            .withDepthStencilState(NO_DEPTH)
            .withCull(false)
            .withLocation("odin/quads_esp")
            .build()
    )

    val QUADS_TRANSLUCENT: RenderPipeline = RenderPipelines.register(
        RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
            .withColorTargetState(TRANSLUCENT)
            .withCull(false)
            .withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR)
            .withPrimitiveTopology(PrimitiveTopology.QUADS)
            .withLocation("odin/quads_translucent")
            .build()
    )

    val QUADS_TRANSLUCENT_ESP: RenderPipeline = RenderPipelines.register(
        RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
            .withDepthStencilState(NO_DEPTH)
            .withColorTargetState(TRANSLUCENT)
            .withCull(false)
            .withLocation("odin/quads_translucent_esp")
            .build()
    )

    val PIPELINE_ROUND_RECT: RenderPipeline = roundRect("round_rect", RenderPipelines.GUI_SNIPPET)
    val PIPELINE_ROUND_RECT_TEXTURED: RenderPipeline = roundRect("round_rect_textured", RenderPipelines.GUI_TEXTURED_SNIPPET)
    val PIPELINE_ROUND_RECT_SHADOW: RenderPipeline = roundRect("round_rect_shadow", RenderPipelines.GUI_SNIPPET)

    private fun roundRect(name: String, snippet: RenderPipeline.Snippet): RenderPipeline = RenderPipelines.register(
        RenderPipeline.builder(snippet)
            .withLocation(Identifier.fromNamespaceAndPath("odin", "pipeline/$name"))
            .withFragmentShader(Identifier.fromNamespaceAndPath("odin", "core/$name"))
            .withVertexShader(Identifier.fromNamespaceAndPath("odin", "core/round_rect"))
            .withVertexBinding(0, RoundedRectRenderer.FORMAT)
            .build()
    )
}