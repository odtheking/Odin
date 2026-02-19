package com.odtheking.odin.events

import com.odtheking.odin.events.core.Event
import com.odtheking.odin.utils.render.RenderConsumer
import net.fabricmc.fabric.api.client.rendering.v1.world.AbstractWorldRenderContext
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldExtractionContext
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext

abstract class RenderEvent(open val context: AbstractWorldRenderContext) : Event() {
    class Extract(override val context: WorldExtractionContext, val consumer: RenderConsumer) : RenderEvent(context)
    class Last(override val context: WorldRenderContext) : RenderEvent(context)
}