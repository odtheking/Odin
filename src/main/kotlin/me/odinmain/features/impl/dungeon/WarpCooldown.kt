package me.odinmain.features.impl.dungeon

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.HudSetting
import me.odinmain.features.settings.impl.StringSetting
import me.odinmain.font.OdinFont
import me.odinmain.utils.clock.Clock
import me.odinmain.utils.render.*
import me.odinmain.utils.skyblock.partyMessage
import java.util.Locale

object WarpCooldown : Module(
    name = "Warp Cooldown",
    description = "Displays the time until you can warp into a dungeon again.",
    category = Category.DUNGEON
) {
    private val announceKick: Boolean by BooleanSetting("Announce Kick", false, description = "Announce when you get kicked from skyblock.")
    private val kickText: String by StringSetting("Kick Text", default = "Kicked!", description = "The text sent in party chat when you get kicked from skyblock.").withDependency { announceKick }
    private val hud by HudSetting("Warp Timer Hud", 10f, 10f, 1f, true) {
        if (it) {
            text("§eWarp: §a30s", 1f, 9f, Color.WHITE, 12f, OdinFont.REGULAR, shadow = true)
            getTextWidth("Warp: 30s", 12f) + 2f to 16f
        } else {
            if (warpTimer.timeLeft() <= 0) return@HudSetting 0f to 0f
            text("§eWarp: §a${String.format(Locale.US, "%.2f", warpTimer.timeLeft() / 1000f)}s", 1f, 9f, Color.WHITE, 12f, OdinFont.REGULAR, shadow = true)
            getTextWidth("§eWarp: §a30s", 12f) + 2f to 12f
        }
    }

    private var warpTimer = Clock(30000)

    init {
        onMessage(Regex("You were kicked while joining that server!"), { enabled && announceKick }) {
            partyMessage(kickText)
        }

        onMessage(Regex("^-*\\n\\[[^]]+] (\\w+) entered (?:MM )?\\w+ Catacombs, Floor (\\w+)!\\n-*$")) {
            warpTimer.updateCD()
        }
    }
}