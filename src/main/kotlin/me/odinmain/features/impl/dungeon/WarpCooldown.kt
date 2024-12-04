package me.odinmain.features.impl.dungeon

import com.github.stivais.aurora.color.Color
import me.odinmain.features.Module
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.StringSetting
import me.odinmain.utils.clock.Clock
import me.odinmain.utils.skyblock.partyMessage
import me.odinmain.utils.ui.TextHUD
import me.odinmain.utils.ui.buildText

object WarpCooldown : Module (
    name = "Warp Cooldown",
    description = "Displays the time until you can warp into a dungeon again."
) {
    private val announceKick: Boolean by BooleanSetting("Announce Kick", false, description = "Announce when you get kicked from skyblock.")
    private val kickText: String by StringSetting("Kick Text", default = "Kicked!", description = "The text sent in party chat when you get kicked from skyblock.").withDependency { announceKick }
    private val showUnit by BooleanSetting("Show unit", default = false, description = "Displays unit of time for the cooldown.").hide()

    private var warpTimer = Clock(30_000L)
    private val HUD by TextHUD("Warp HUD") { color, font, shadow ->
        needs { lastUpdate - System.currentTimeMillis() >= 0 }
        buildText(
            string = "Warp:",
            supplier = { "${if (preview) "30" else (lastUpdate - System.currentTimeMillis()) / 1000}${if (showUnit) "s" else ""}" },
            font, color, Color.WHITE, shadow
        )
    }.registerSettings(::showUnit).setting(description = "Displays the cooldown.")

    private var lastUpdate: Long = System.currentTimeMillis()

    init {
        onMessage(Regex("You were kicked while joining that server!"), { enabled && announceKick }) {
            partyMessage(kickText)
        }

        onMessage(Regex("^-*\\n\\[[^]]+] (\\w+) entered (?:MM )?\\w+ Catacombs, Floor (\\w+)!\\n-*$")) {
            warpTimer.updateCD()
        }
    }
}