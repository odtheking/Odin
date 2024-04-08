package me.odinclient.features.impl.render

import me.odinmain.features.Category
import me.odinmain.features.Module
import net.minecraftforge.client.event.RenderBlockOverlayEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object SeeThroughBlocks : Module(
    name = "SeeThroughBlocks",
    category = Category.RENDER,
    description = "Allows to see through blocks when you're in them"
) {
    @SubscribeEvent
    fun onRenderBlockOverlay(event: RenderBlockOverlayEvent) {
        if (event.overlayType == RenderBlockOverlayEvent.OverlayType.BLOCK) event.setCanceled(true)
    }
}