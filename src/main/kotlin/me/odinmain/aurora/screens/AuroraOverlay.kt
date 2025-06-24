package me.odinmain.aurora.screens

import com.github.stivais.aurora.Aurora
import me.odinmain.events.impl.RenderOverlayNoCaching
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

class AuroraOverlay(val instance: Aurora.Instance) {

    fun open() {
        MinecraftForge.EVENT_BUS.register(this)
    }

    fun close() {
        MinecraftForge.EVENT_BUS.unregister(this)
    }

    @SubscribeEvent
    fun onRender(event: RenderOverlayNoCaching) {
        instance.render()
    }
}