package com.odtheking.odin.utils.render

import net.minecraft.client.renderer.rendertype.LayeringTransform
import net.minecraft.client.renderer.rendertype.OutputTarget
import net.minecraft.client.renderer.rendertype.RenderSetup
import net.minecraft.client.renderer.rendertype.RenderType

object CustomRenderType {

    // RenderTypes.LINES, RenderTypes.LINES_TRANSLUCENT || LINES_ESP, LINES_TRANSLUCENT_ESP

    val LINES_ESP: RenderType = RenderType.create(
        "lines-esp",
        RenderSetup.builder(CustomRenderPipelines.LINES_ESP)
            .setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
            .setOutputTarget(OutputTarget.ITEM_ENTITY_TARGET)
            .createRenderSetup()
    )

    val LINES_TRANSLUCENT_ESP: RenderType = RenderType.create(
        "lines-translucent-esp",
        RenderSetup.builder(CustomRenderPipelines.LINES_TRANSLUCENT_ESP)
            .setOutputTarget(OutputTarget.ITEM_ENTITY_TARGET)
            .createRenderSetup()
    )

    // RenderTypes.DEBUG_FILLED_BOX || QUADS_ESP

    val QUADS_ESP: RenderType = RenderType.create(
        "quads-esp",
        RenderSetup.builder(CustomRenderPipelines.QUADS_ESP)
            .sortOnUpload()
            .setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
            .createRenderSetup()
    )
}