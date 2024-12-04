package me.odinmain.features.impl.render

import me.odinmain.features.Module
import me.odinmain.utils.ServerUtils
import me.odinmain.utils.ui.Colors
import me.odinmain.utils.ui.TextHUD
import me.odinmain.utils.ui.buildText

object ServerHud : Module(
    name = "Performance Display",
    description = "Displays certain performance-related metrics, like ping, TPS and FPS."
) {
    private val fpsHUD by TextHUD("FPS") { color, font, shadow ->
        buildText(
            string = "FPS:", supplier = { getFPS() },
            font = font, color1 = color, color2 = Colors.WHITE, shadow
        )
    }.setting("Display's your fps on screen.")

    private val pingHUD by TextHUD("Ping") { color, font, shadow ->
        buildText(
            string = "Ping:", supplier = { ServerUtils.averagePing.toInt() },
            font = font, color1 = color, color2 = Colors.WHITE, shadow
        )
    }.setting("Display's your ping on screen.")

    private val tpsHUD by TextHUD("TPS") { color, font, shadow ->
        buildText(
            string = "TPS:", supplier = { ServerUtils.averageTps.toInt() },
            font = font, color1 = color, color2 = Colors.WHITE, shadow
        )
    }.setting("Display's your tps on screen.")

    fun getFPS() = mc.debug.split(" ")[0].toIntOrNull() ?: 0
}