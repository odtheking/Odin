package me.odinmain.features.impl.dungeon

import me.odinmain.features.Module
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.HudSetting
import me.odinmain.features.settings.impl.StringSetting
import me.odinmain.utils.clock.Clock
import me.odinmain.utils.render.RenderUtils
import me.odinmain.utils.render.getMCTextWidth
import me.odinmain.utils.skyblock.partyMessage
import me.odinmain.utils.toFixed
import me.odinmain.utils.ui.Colors

object WarpCooldown : Module(
    name = "Warp Cooldown",
    desc = "Displays the time until you can warp into a new dungeon."
) {
    private val announceKick: Boolean by BooleanSetting("Announce Kick", false, desc = "Announce when you get kicked from skyblock.")
    private val kickText: String by StringSetting("Kick Text", "Kicked!", desc = "The text sent in party chat when you get kicked from skyblock.").withDependency { announceKick }
    private val hud by HudSetting("Warp Timer Hud", 10f, 10f, 1f, true) {
        if (it) {
            RenderUtils.drawText("§eWarp: §a30s", 1f, 1f, 1f, Colors.WHITE, shadow = true)
            getMCTextWidth("Warp: 30s") + 2f to 12f
        } else {
            if (warpTimer.timeLeft() <= 0) return@HudSetting 0f to 0f
            RenderUtils.drawText("§eWarp: §a${(warpTimer.timeLeft() / 1000f).toFixed()}s", 1f, 1f, 1f, Colors.WHITE, shadow = true)
            getMCTextWidth("§eWarp: §a30s") + 2f to 12f
        }
    }

    private var warpTimer = Clock(30_000L)

    init {
        onMessage(Regex("^You were kicked while joining that server!$"), { enabled && announceKick }) {
            partyMessage(kickText)
        }

        onMessage(Regex("^-*\\n\\[[^]]+] (\\w+) entered (?:MM )?\\w+ Catacombs, Floor (\\w+)!\\n-*$")) {
            warpTimer.updateCD()
        }
    }
}