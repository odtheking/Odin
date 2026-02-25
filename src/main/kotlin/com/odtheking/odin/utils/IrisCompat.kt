package com.odtheking.odin.utils

import com.mojang.blaze3d.pipeline.RenderPipeline
import com.odtheking.odin.utils.render.CustomRenderLayer
import net.fabricmc.loader.api.FabricLoader
import net.irisshaders.iris.api.v0.IrisApi
import net.irisshaders.iris.api.v0.IrisProgram
import net.minecraft.client.renderer.rendertype.RenderType

/**
 * Created by @j10a1n15
 */
interface IrisCompatability {
    fun registerPipeline(pipeline: RenderPipeline, shaderType: IrisShaderType) {}
    fun registerRenderType(pipeline: RenderType, shaderType: IrisShaderType) {
        registerPipeline(pipeline.pipeline(), shaderType)
    }

    companion object : IrisCompatability by resolve() {
        init {
            registerRenderType(CustomRenderLayer.LINE_LIST, IrisShaderType.LINES)
            registerRenderType(CustomRenderLayer.LINE_LIST_ESP, IrisShaderType.LINES)
            registerRenderType(CustomRenderLayer.TRIANGLE_STRIP, IrisShaderType.BASIC)
            registerRenderType(CustomRenderLayer.TRIANGLE_STRIP_ESP, IrisShaderType.BASIC)
        }
    }
}

enum class IrisShaderType {
    LINES,
    BASIC,
}

internal object IrisCompatImpl : IrisCompatability {
    private val instance by lazy { IrisApi.getInstance() }

    override fun registerPipeline(pipeline: RenderPipeline, shaderType: IrisShaderType) {
        val type = when (shaderType) {
            IrisShaderType.BASIC -> IrisProgram.BASIC
            IrisShaderType.LINES -> IrisProgram.LINES
        }
        instance.assignPipeline(pipeline, type)
    }
}


internal object IrisCompatNoOp : IrisCompatability

internal fun resolve(): IrisCompatability = if (FabricLoader.getInstance().isModLoaded("iris")) IrisCompatImpl else IrisCompatNoOp