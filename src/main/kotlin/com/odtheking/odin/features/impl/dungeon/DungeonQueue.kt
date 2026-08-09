package com.odtheking.odin.features.impl.dungeon

import com.odtheking.odin.clickgui.settings.Setting.Companion.withDependency
import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.clickgui.settings.impl.NumberSetting
import com.odtheking.odin.events.ChatPacketEvent
import com.odtheking.odin.events.PartyEvent
import com.odtheking.odin.events.LevelEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.handlers.schedule
import com.odtheking.odin.utils.render.textDim
import com.odtheking.odin.utils.sendCommand
import com.odtheking.odin.utils.toFixed

object DungeonQueue : Module(
    name = "Dungeon Queue",
    description = "Automates dungeon requeuing and displays queue cooldowns."
) {
    private val hud by HUD("Warp Cooldown", "Displays the warp timer in the HUD.") {
        if (warpTimer - System.currentTimeMillis() <= 0 && !it) return@HUD 0 to 0
        textDim("§eWarp: §a${if (it) "30" else ((warpTimer - System.currentTimeMillis()) / 1000f).toFixed()}s", 0, 0, Colors.WHITE)
    }
    private val announceKick by BooleanSetting("Announce Kick", false, desc = "Announce when you get kicked from skyblock.")

    private val autoRequeue by BooleanSetting("Auto Requeue", false, desc = "Automatically starts a new dungeon at the end of a dungeon.")
    private val requeueDelay by NumberSetting("Requeue Delay", 2, 0, 30, 1, desc = "The delay in seconds before requeuing.", unit = "s").withDependency { autoRequeue }
    private val disablePartyLeave by BooleanSetting("Disable on leave/kick", true, desc = "Disables the requeue on party leave message.").withDependency { autoRequeue }

    private val enterRegex = Regex("^-*\\n\\[[^]]+] (\\w+) entered (?:MM )?\\w+ Catacombs, Floor (\\w+)!\\n-*$")
    private val kickedInstanceRegex = Regex("^You are no longer allowed to access this instance!$")
    private val kickedJoiningRegex = Regex("^You were kicked while joining that server!$")
    private val extraStatsRegex = Regex(" {29}> EXTRA STATS <")

    private var warpTimer = 0L
    var disableRequeue = false

    init {
        on<ChatPacketEvent> {
            when {
                announceKick && (value.matches(kickedJoiningRegex) || value.matches(kickedInstanceRegex)) -> sendCommand("pc I was kicked!")
                value.matches(enterRegex) -> warpTimer = System.currentTimeMillis() + 30_000L
                autoRequeue && value.matches(extraStatsRegex) -> {
                    if (disableRequeue.also { disableRequeue = false }) return@on

                    schedule(requeueDelay * 20) {
                        if (!disableRequeue) sendCommand("instancerequeue")
                    }
                }
            }
        }

        on<PartyEvent.Leave> {
            if (autoRequeue && disablePartyLeave) disableRequeue = true
        }

        on<LevelEvent.Load> {
            disableRequeue = false
        }
    }
}