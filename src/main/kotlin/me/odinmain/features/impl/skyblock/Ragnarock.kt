package me.odinmain.features.impl.skyblock

import me.odinmain.features.Module
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.utils.skyblock.*
import net.minecraft.network.play.server.S29PacketSoundEffect

object Ragnarock : Module(
    name = "Ragnarock",
    desc = "Provides alerts about Ragnarock's state."
) {
    private val alert by BooleanSetting("Alert", true, desc = "Alerts you when you start casting Ragnarock.")
    private val alertCancelled by BooleanSetting("Alert Cancelled", true, desc = "Alerts you when your Ragnarock is cancelled.")
    private val strengthGainedMessage by BooleanSetting("Strength Gained", true, desc = "Sends a mod message which will notify of strength gained from Ragnarock after casting")
    private val announceStrengthGained by BooleanSetting("Send to party", false, desc = "Sends party message of strength gained after casting").withDependency { strengthGainedMessage }

    init {
        onMessage(Regex("Ragnarock was cancelled due to (?:being hit|taking damage)!")) {
            if (alertCancelled) PlayerUtils.alert("§cRagnarock Cancelled")
        }

        onPacket<S29PacketSoundEffect> {
            if (it.soundName != "mob.wolf.howl" || it.pitch != 1.4920635f || !isHolding("RAGNAROCK_AXE")) return@onPacket
            if (alert) PlayerUtils.alert("§aCasted Ragnarock")
            val strengthGain = ((mc.thePlayer?.heldItem?.getSBStrength ?: return@onPacket) * 1.5).toInt()
            if (strengthGainedMessage) modMessage("§7Gained strength: §4$strengthGain")
            if (announceStrengthGained) partyMessage("Gained strength from Ragnarock: $strengthGain")
        }
    }
}