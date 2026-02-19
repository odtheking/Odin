package com.odtheking.odin.features.impl.skyblock

import com.odtheking.odin.clickgui.settings.Setting.Companion.withDependency
import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.events.ChatPacketEvent
import com.odtheking.odin.events.PlaySoundEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.*
import net.minecraft.sounds.SoundEvents

object Ragnarock : Module(
    name = "Ragnarock",
    description = "Alerts when you cast the Ragnarock or it gets cancelled."
) {
    private val castAlert by BooleanSetting("Cast alert", true, "Alerts when you cast Ragnarock.")
    private val cancelAlert by BooleanSetting("Cancel alert", true, "Alerts when Ragnarock is cancelled.")
    private val strengthGainedMessage by BooleanSetting("Strength gained", true, "Shows Ragnarock strength gained.")
    private val announceStrengthGained by BooleanSetting("Announce gained strength", false, "Announce gained strength in party chat").withDependency { strengthGainedMessage }

    private val cancelRegex = Regex("Ragnarock was cancelled due to (?:being hit|taking damage)!")

    init {
        on<ChatPacketEvent> {
            if (cancelAlert && value.matches(cancelRegex)) alert("§cRagnarock Cancelled!")
        }

        on<PlaySoundEvent> {
            if (pitch == 1.4920635f && mc.player?.mainHandItem?.itemId == "RAGNAROCK_AXE" &&
                SoundEvents.WOLF_SOUNDS.entries.any { it.value.deathSound.value().location == sound.location }
            ) {
                if (castAlert) alert("§aCasted Rag")
                val strengthGained = ((mc.player?.mainHandItem?.strength ?: return@on) * 1.5).toInt()
                if (strengthGainedMessage) {
                    modMessage("§7Gained strength: §4$strengthGained")
                    if (announceStrengthGained) {
                        sendCommand("pc Gained strength from Ragnarock: $strengthGained")
                    }
                }
            }
        }
    }
}