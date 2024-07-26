package me.odinmain.features.impl.dungeon

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.HudSetting
import me.odinmain.font.OdinFont
import me.odinmain.ui.hud.HudElement
import me.odinmain.utils.clock.Clock
import me.odinmain.utils.render.*

object WarpCooldown : Module(
    name = "Warp Cooldown",
    description = "Displays the time until you can warp into a dungeon again.",
    category = Category.DUNGEON
) {
    private val hud: HudElement by HudSetting("Warp Timer Hud", 10f, 10f, 1f, true) {
        if (it) {
            text("§eWarp: §a30s", 1f, 9f, Color.WHITE, 12f, OdinFont.REGULAR, shadow = true)
            getTextWidth("Warp: 30s", 12f) + 2f to 16f
        } else {
            if (warpTimer.timeLeft() <= 0) return@HudSetting 0f to 0f
            text("§eWarp: §a${String.format("%.2f", warpTimer.timeLeft().toFloat() / 1000)}s", 1f, 9f, Color.WHITE, 12f, OdinFont.REGULAR, shadow = true)
            getTextWidth("§eWarp: §a30s", 12f) + 2f to 12f
        }
    }

    private var warpTimer = Clock(30000)

    init {
        onMessage(Regex("^-*>newLine<-\\[[^]]+] (\\w+) entered \\w+ Catacombs, Floor (\\w+)!->newLine<-*$")) {
            warpTimer.updateCD()
        }
    }
}