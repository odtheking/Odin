package me.odinmain.features.impl.skyblock

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.utils.skyblock.*
import net.minecraft.network.play.server.S29PacketSoundEffect

object RagnarockAxe : Module(
    name = "Rag Axe",
    description = "Tracks rag axe cooldowns.",
    category = Category.SKYBLOCK
) {
    private val alert by BooleanSetting("Alert", true, description = "Alerts you when you start casting rag axe.")
    private val alertCancelled by BooleanSetting("Alert Cancelled", true, description = "Alerts you when your rag axe is cancelled.")
    private val strengthGainedMessage by BooleanSetting("Strength Gained", true, description = "Sends a mod message which will notify of strength gained from rag axe after casting")
    private val announceStrengthGained by BooleanSetting("Send to party", false, description = "Sends party message of strength gained after casting").withDependency { strengthGainedMessage }

    init {
        onMessage(Regex("Ragnarock was cancelled due to (?:being hit|taking damage)!")) {
            if (alertCancelled) PlayerUtils.alert("§cRag Axe Cancelled")
        }

        onPacket(S29PacketSoundEffect::class.java) {
            if (it.soundName != "mob.wolf.howl" || it.pitch != 1.4920635f || !isHolding("RAGNAROCK_AXE")) return@onPacket
            if (alert) PlayerUtils.alert("§aCasted Rag Axe")
            val strengthGain = ((mc.thePlayer?.heldItem?.getSBStrength ?: return@onPacket) * 1.5).toInt()
            if (strengthGainedMessage) modMessage("§7Gained strength: §4$strengthGain")
            if (announceStrengthGained) partyMessage("Gained strength from Ragnarock Axe: $strengthGain")
        }
    }
}