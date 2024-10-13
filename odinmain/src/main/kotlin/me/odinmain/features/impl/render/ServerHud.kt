@file:Suppress("UNUSED")

package me.odinmain.features.impl.render

import com.github.stivais.ui.constraints.percent
import com.github.stivais.ui.constraints.px
import me.odinmain.features.Module
import me.odinmain.utils.ServerUtils
import me.odinmain.utils.ui.TextHUD
import me.odinmain.utils.ui.and

object ServerHud : Module(
    name = "Performance Display",
    description = "Displays certain performance-related metrics, like ping, TPS and FPS."
) {
    private val fpsHUD by TextHUD(2.percent, 2.percent) { color, font ->
        text(
            text = "FPS ",
            color = color,
            size = 30.px
        ) and text({ getFPS() }, font = font)
    }.setting("FPS")

    private val pingHUD by TextHUD(8.percent, 2.percent) { color, font ->
        text(
            text = "FPS ",
            color = color,
            size = 30.px
        ) and text({ ServerUtils.averagePing.toInt() }, font = font)
    }.setting("FPS")

    private val tpsHUD by TextHUD(14.percent, 2.percent) { color, font ->
        text(
            text = "TPS ",
            color = color,
            size = 30.px,
            font = font,
        ) and text({ ServerUtils.averageTps.toInt() }, font = font)
    }.setting("TPS")

    fun getFPS() = mc.debug.split(" ")[0].toIntOrNull() ?: 0
}