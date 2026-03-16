package com.odtheking.odin.utils.render

import com.mojang.blaze3d.pipeline.RenderPipeline
import com.mojang.blaze3d.platform.DepthTestFunction
import net.minecraft.client.renderer.RenderPipelines

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
}