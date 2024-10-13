package me.odinmain.features.impl.dungeon

import com.github.stivais.ui.constraints.percent
import com.github.stivais.ui.constraints.px
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.utils.ui.TextHUD
import me.odinmain.utils.ui.and

object WarpCooldown : Module (
    name = "Warp Cooldown",
    description = "Displays the time until you can warp into a dungeon again."
) {
    private val showUnit by BooleanSetting("Show unit", default = false, description = "Displays unit of time for the cooldown.").hide()

    private val HUD by TextHUD(
        2.5.percent,
        2.5.percent,
    ) { color, font ->
        if (preview) {
            text(
                "Warp ",
                color = color,
                font = font,
                size = 30.px
            ) and text("30${if (showUnit) "s" else ""}", font = font)
        } else {
            needs { lastUpdate - System.currentTimeMillis() >= 0 }
            text(
                "Warp ",
                color = color,
                font = font,
                size = 30.px
            ) and text({ "${(lastUpdate - System.currentTimeMillis()) / 1000}${if (showUnit) "s" else ""}" }, font = font)
        }
    }.setting(
        ::showUnit
    ).setting("Warp HUD")

    private var lastUpdate: Long = System.currentTimeMillis()

    init {
        onMessage(Regex("(?s)^.*\\[[^]]+] (\\w+) entered \\w+ Catacombs, Floor (\\w+)!.*\$")) {
            if (!it.startsWith("-----------------------------") && !it.endsWith("-----------------------------")) return@onMessage
            lastUpdate = System.currentTimeMillis() + 30_000
        }
    }
}