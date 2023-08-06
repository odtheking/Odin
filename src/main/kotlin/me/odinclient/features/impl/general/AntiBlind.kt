package me.odinclient.features.impl.general

import me.odinclient.features.Category
import me.odinclient.features.Module
import net.minecraft.client.renderer.GlStateManager
import net.minecraftforge.client.event.EntityViewRenderEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object AntiBlind : Module(
    "No Blindness",
    category = Category.GENERAL,
    description = "Disables blindness"
) {
    @SubscribeEvent
    fun onRenderFog(event: EntityViewRenderEvent.FogDensity) {
        event.density = 0f
        GlStateManager.setFogStart(998f)
        GlStateManager.setFogEnd(999f)
        event.isCanceled = true
    }
}