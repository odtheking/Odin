package com.odtheking.odin.features.impl.nether

import com.odtheking.odin.clickgui.settings.Setting.Companion.withDependency
import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.clickgui.settings.impl.NumberSetting
import com.odtheking.odin.events.ChatPacketEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.handlers.schedule
import com.odtheking.odin.utils.sendCommand

object Misc : Module(
    name = "Misc",
    description = "Miscellaneous Nether features."
) {
    private val manaDrain by BooleanSetting("Mana Drain", true, desc = "Sends in party chat when you drain mana near players.")
    private val autoRequeue by BooleanSetting("Auto Requeue", true, desc = "Automatically requeues you after a Kuudra run.")
    private val requeueDelay by NumberSetting("Requeue Delay", 20, 0..100, 1, unit = "ticks", desc = "Delay before requeuing after a run ends.").withDependency { autoRequeue }

    private val endRunRegex =
        Regex("^\\[NPC] Elle: Good job everyone. A hard fought battle come to an end. Let's get out of here before we run into any more trouble!$")
    private val endStoneRegex = Regex("^Used Extreme Focus! \\((\\d+) Mana\\)$")

    init {
        on<ChatPacketEvent> {
            if (manaDrain) endStoneRegex.find(value)?.groupValues?.getOrNull(1)?.let { mana ->
                val players =
                    mc.level?.players()?.filter { it.distanceToSqr(mc.player ?: return@on) < 49 && it.uuid.version() == 4 } ?: return@on
                sendCommand("pc Used $mana mana (${players.size} players nearby)")
            }

            if (autoRequeue && endRunRegex.matches(value))
                schedule(requeueDelay) { sendCommand("instancerequeue") }
        }
    }
}