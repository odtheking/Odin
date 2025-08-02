package me.odinmain.features.impl.dungeon

import me.odinmain.clickgui.settings.Setting.Companion.withDependency
import me.odinmain.clickgui.settings.impl.BooleanSetting
import me.odinmain.clickgui.settings.impl.StringSetting
import me.odinmain.features.Module
import me.odinmain.utils.clock.Clock
import me.odinmain.utils.render.Colors
import me.odinmain.utils.skyblock.partyMessage
import me.odinmain.utils.toFixed
import me.odinmain.utils.ui.drawStringWidth

object WarpCooldown : Module(
    name = "Warp Cooldown",
    description = "Displays the time until you can warp into a new dungeon."
) {
    private val announceKick by BooleanSetting("Announce Kick", false, desc = "Announce when you get kicked from skyblock.")
    private val kickText by StringSetting("Kick Text", "Kicked!", desc = "The text sent in party chat when you get kicked from skyblock.").withDependency { announceKick }
    private val hud by HUD("Warp Timer Hud", "Displays the warp timer in the HUD.") {
        if (warpTimer.timeLeft() <= 0 && !it) return@HUD 0f to 0f
        drawStringWidth("§eWarp: §a${if (it) "30" else (warpTimer.timeLeft() / 1000f).toFixed()}s", 1f, 1f, Colors.WHITE) + 2f to 10f
    }

    private var warpTimer = Clock(30_000L)

    init {
        onMessage(Regex("^You were kicked while joining that server!$"), { enabled && announceKick }) {
            partyMessage(kickText)
        }

        onMessage(Regex("^You are no longer allowed to access this instance!$"), { enabled && announceKick }) {
            partyMessage(kickText)
        }

        onMessage(Regex("^-*\\n\\[[^]]+] (\\w+) entered (?:MM )?\\w+ Catacombs, Floor (\\w+)!\\n-*$")) {
            warpTimer.updateCD()
        }
    }
}