package com.odtheking.odin.utils.render

import com.mojang.blaze3d.pipeline.BlendFunction
import com.mojang.blaze3d.pipeline.ColorTargetState
import com.mojang.blaze3d.pipeline.RenderPipeline
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.resources.Identifier
import java.util.*

object CustomRenderPipelines {
    val LINES_ESP: RenderPipeline = RenderPipelines.register(
        RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)
            .withDepthStencilState(Optional.empty())
            .withLocation("odin/lines_esp")
            .build()
    )

    val LINES_TRANSLUCENT_ESP: RenderPipeline = RenderPipelines.register(
        RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)
            .withDepthStencilState(Optional.empty())
            .withLocation("odin/lines_translucent_esp")
            .build()
    )

    val QUADS_ESP: RenderPipeline = RenderPipelines.register(
        RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
            .withDepthStencilState(Optional.empty())
            .withLocation("odin/quads_esp")
            .build()
    )

    val PIPELINE_ROUND_RECT: RenderPipeline = roundRect("round_rect", RenderPipelines.GUI_SNIPPET, "round_rect")
    val PIPELINE_ROUND_RECT_TEXTURED: RenderPipeline = roundRect("round_rect_textured", RenderPipelines.GUI_TEXTURED_SNIPPET, "round_rect")
    val PIPELINE_ROUND_RECT_SHADOW: RenderPipeline = roundRect("round_rect_shadow", RenderPipelines.GUI_SNIPPET, "round_rect_shadow")

    private fun roundRect(name: String, snippet: RenderPipeline.Snippet, vertex: String): RenderPipeline = RenderPipelines.register(
        RenderPipeline.builder(snippet)
            .withLocation(Identifier.fromNamespaceAndPath("odin", "pipeline/$name"))
            .withFragmentShader(Identifier.fromNamespaceAndPath("odin", "core/$name"))
            .withVertexShader(Identifier.fromNamespaceAndPath("odin", "core/$vertex"))
            .withVertexBinding(0, RoundedRectRenderer.FORMAT)
            .build()
    )
}