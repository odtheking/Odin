package com.odtheking.odin.utils.render

import net.minecraft.client.renderer.rendertype.LayeringTransform
import net.minecraft.client.renderer.rendertype.RenderSetup
import net.minecraft.client.renderer.rendertype.RenderType

object CustomRenderLayer {

    val LINE_LIST: RenderType = RenderType.create(
        "line-list",
        RenderSetup.builder(CustomRenderPipelines.LINE_LIST)
            .bufferSize(RenderType.BIG_BUFFER_SIZE)
            .setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
            .createRenderSetup()
    )

    val LINE_LIST_ESP: RenderType = RenderType.create(
        "line-list-esp",
        RenderSetup.builder(CustomRenderPipelines.LINE_LIST_ESP)
            .bufferSize(RenderType.BIG_BUFFER_SIZE)
            .setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
            .createRenderSetup()
    )

    val TRIANGLE_STRIP: RenderType = RenderType.create(
        "triangle_strip",
        RenderSetup.builder(CustomRenderPipelines.TRIANGLE_STRIP)
            .bufferSize(RenderType.BIG_BUFFER_SIZE)
            .setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
            .createRenderSetup()
    )

    val TRIANGLE_STRIP_ESP: RenderType = RenderType.create(
        "triangle_strip_esp",
        RenderSetup.builder(CustomRenderPipelines.TRIANGLE_STRIP_ESP)
            .bufferSize(RenderType.BIG_BUFFER_SIZE)
            .setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
            .createRenderSetup()
    )
}