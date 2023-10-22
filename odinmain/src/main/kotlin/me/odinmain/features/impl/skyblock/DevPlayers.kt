package me.odinmain.features.impl.skyblock

import net.minecraft.client.renderer.GlStateManager
import net.minecraftforge.client.event.RenderPlayerEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object DevPlayers {

    data class PlayerSize(val xScale: Float, val yScale: Float, val zScale: Float)

    val devs = hashMapOf(
        "_Inton_" to PlayerSize(2f, .5f, 2f),
        "OdinClient" to PlayerSize(2f, .5f, 1f),
        "Odtheking" to PlayerSize(2f, .5f, 2f),
        "Stivais" to PlayerSize(2f, 1f, 1f),
        "saksiq" to PlayerSize(2f, 1f, 1f),
        "stiff_maister89" to PlayerSize(1f, 1f, 1f),
    )

    @SubscribeEvent
    fun onRenderPlayer(event: RenderPlayerEvent.Pre) {
        if (!devs.containsKey(event.entity.name)) return
        val size = devs[event.entity.name]!!
        GlStateManager.pushMatrix()
        GlStateManager.scale(size.xScale, size.yScale, size.zScale)
    }

    @SubscribeEvent
    fun onRenderPlayer(event: RenderPlayerEvent.Post) {
        if (!devs.containsKey(event.entity.name)) return
        GlStateManager.popMatrix()
    }
}