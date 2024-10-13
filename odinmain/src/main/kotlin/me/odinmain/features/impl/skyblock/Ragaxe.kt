package me.odinmain.features.impl.skyblock

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.utils.runIn
import me.odinmain.utils.skyblock.PlayerUtils
import me.odinmain.utils.skyblock.heldItem
import me.odinmain.utils.skyblock.strength
import me.odinmain.utils.skyblock.modMessage
import me.odinmain.utils.skyblock.partyMessage

object Ragaxe : Module(
    name = "Rag Axe",
    description = "Tracks rag axe cooldowns.",
    category = Category.SKYBLOCK
) {
    private val alert by BooleanSetting("Alert", true, description = "Alerts you when you start casting rag axe.")
    private val alertCancelled by BooleanSetting("Alert Cancelled", true, description = "Alerts you when your rag axe is cancelled.")
    private val strengthGainedMessage by BooleanSetting("Strength Gained", true, description = "Sends a mod message which will notify of strength gained from rag axe after casting")
    private val announceStrengthGained by BooleanSetting("Send to party", true, description = "Sends party message of strength gained after casting").withDependency { strengthGainedMessage }

    var cancelled = false
    var casting = false

    init {
        onMessage(Regex("^.+CASTING IN 3s(.+)?\$"), { alert && enabled && !casting }) {
            casting = true
            runIn(62) {
                if (cancelled) {
                    cancelled = false
                    casting = false
                    return@runIn
                }
                PlayerUtils.alert("§aCasted Rag Axe")
                val strengthGain = ((heldItem?.strength ?: 0) * 1.5).toInt()
                if (strengthGainedMessage) modMessage("Gained strength: $strengthGain")
                if (announceStrengthGained) partyMessage("Gained strength from rag axe: $strengthGain")
                casting = false
            }
        }

        onMessage(Regex("Ragnarock was cancelled due to (?:being hit|taking damage)!"), { alertCancelled && enabled }) {
            PlayerUtils.alert("§cRag Axe Cancelled")
            cancelled = true
            casting = false
        }

        onWorldLoad {
            cancelled = false
            casting = false
        }
    }

}