package me.odinmain.features.impl.skyblock


import net.minecraft.client.entity.AbstractClientPlayer
import net.minecraft.client.renderer.GlStateManager

object DevPlayers {

    data class PlayerSize(val xScale: Float, val yScale: Float, val zScale: Float)

    val devs = hashMapOf(
        "_Inton_" to PlayerSize(2f, .5f, 2f),
        "odthebestest" to PlayerSize(0.1f, 0.1f, 0.1f),
        "Odtheking" to PlayerSize(3f, 3f, 3f),
        "Stivais" to PlayerSize(2f, 1f, 1f),
        "saksiq" to PlayerSize(2f, 1f, 1f),
        "stiff_maister89" to PlayerSize(1f, 1f, 1f),

    )

    fun preRenderCallbackScaleHook(entityLivingBaseIn: AbstractClientPlayer ) {
        if (!devs.containsKey(entityLivingBaseIn.name)) return
        val dev = devs[entityLivingBaseIn.name]
        if (dev != null) { GlStateManager.scale(dev.xScale, dev.yScale, dev.zScale) }
    }
}