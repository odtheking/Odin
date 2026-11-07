package com.odtheking.odin.utils.render

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.renderer.SubmitNodeCollector
import net.minecraft.client.renderer.feature.FeatureFrameContext
import net.minecraft.client.renderer.feature.FeatureRendererType
import net.minecraft.client.renderer.feature.RenderTypeFeatureRenderer
import net.minecraft.client.renderer.feature.submit.TranslucentSubmit
import net.minecraft.client.renderer.rendertype.RenderType

class NoDepthSubmit(
    val pose: PoseStack.Pose,
    val renderType: RenderType,
    val renderer: SubmitNodeCollector.CustomGeometryRenderer
) : TranslucentSubmit {
    override fun distanceToCameraSq(): Float = Float.MAX_VALUE
    override fun featureType(): FeatureRendererType<NoDepthSubmit> = NoDepthFeatureRenderer.TYPE
}

class NoDepthFeatureRenderer : RenderTypeFeatureRenderer<NoDepthSubmit>() {
    override fun buildGroup(context: FeatureFrameContext, submits: List<NoDepthSubmit>) {
        for (submit in submits) submit.renderer.render(submit.pose, getVertexBuilder(submit.renderType))
    }

    companion object {
        val TYPE: FeatureRendererType<NoDepthSubmit> = FeatureRendererType.create("odin_no_depth")
    }
}