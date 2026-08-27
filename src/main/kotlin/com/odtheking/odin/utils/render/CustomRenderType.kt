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

    // RenderTypes.DEBUG_FILLED_BOX / RenderTypes.debugFilledBox() || QUADS_OPAQUE / QUADS_TRANSLUCENT / QUADS_ESP / QUADS_TRANSLUCENT_ESP

    val QUADS_OPAQUE: RenderType = RenderType.create(
        "quads-opaque",
        RenderSetup.builder(CustomRenderPipelines.QUADS_OPAQUE)
            .createRenderSetup()
    )

    val QUADS_TRANSLUCENT: RenderType = RenderType.create(
        "quads-translucent",
        RenderSetup.builder(CustomRenderPipelines.QUADS_TRANSLUCENT)
            .sortOnUpload()
            .createRenderSetup()
    )

    val QUADS_ESP: RenderType = RenderType.create(
        "quads-esp",
        RenderSetup.builder(CustomRenderPipelines.QUADS_ESP)
            .createRenderSetup()
    )

    val QUADS_TRANSLUCENT_ESP: RenderType = RenderType.create(
        "quads-translucent-esp",
        RenderSetup.builder(CustomRenderPipelines.QUADS_TRANSLUCENT_ESP)
            .sortOnUpload()
            .createRenderSetup()
    )
}