package me.odinmain.features.impl.render

import me.odinmain.features.Module

object ServerHud : Module(
    name = "Performance Display",
    description = "Displays certain performance-related metrics, like ping, TPS and FPS."
) {

    fun getFPS() = mc.debug.split(" ")[0].toIntOrNull() ?: 0
}