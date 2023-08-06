package me.odinclient.features.impl.general

import me.odinclient.features.Category
import me.odinclient.features.Module
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object AntiPortal : Module(
    "No Portal Effect",
    category = Category.GENERAL,
    description = "Disables the nether portal overlay."
) {
    @SubscribeEvent
    fun onOverlay(event: RenderGameOverlayEvent.Pre) {
        if (event.type == RenderGameOverlayEvent.ElementType.PORTAL) {
            event.isCanceled = true
        }
    }
}