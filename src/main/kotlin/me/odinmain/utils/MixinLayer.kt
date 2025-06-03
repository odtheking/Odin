package me.odinmain.utils

import net.minecraft.client.renderer.EntityRenderer
import net.minecraft.client.renderer.entity.RenderManager

// layer to call accessors and invokers from odinmain
interface PrivateMethodInvoker {
    fun invokeSetupCameraTransform(instance: EntityRenderer, partialTicks: Float, renderPass: Int)
    fun getRenderPosX(instance: RenderManager): Double
    fun getRenderPosY(instance: RenderManager): Double
    fun getRenderPosZ(instance: RenderManager): Double
}

object PrivateMethodAccess {
    var impl: PrivateMethodInvoker? = null
}

fun EntityRenderer.setupCameraTransform(partialTicks: Float, renderPass: Int) {
    PrivateMethodAccess.impl?.invokeSetupCameraTransform(this, partialTicks, renderPass)
}

fun RenderManager.getRenderPosX(): Double {
    return PrivateMethodAccess.impl?.getRenderPosX(this) ?: 0.0
}
fun RenderManager.getRenderPosY(): Double {
    return PrivateMethodAccess.impl?.getRenderPosY(this) ?: 0.0
}
fun RenderManager.getRenderPosZ(): Double {
    return PrivateMethodAccess.impl?.getRenderPosZ(this) ?: 0.0
}