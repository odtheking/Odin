package me.odinmain.features.impl.skyblock

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.utils.skyblock.PlayerUtils

object Ragaxe : Module(
    name = "Rag Axe",
    description = "Tracks rag axe cooldowns.",
    category = Category.SKYBLOCK
) {
    private val alert: Boolean by BooleanSetting("Alert", true, description = "Alerts you when you start casting rag axe.")
    private val alertCancelled: Boolean by BooleanSetting("Alert Cancelled", true, description = "Alerts you when your rag axe is cancelled.")

    init {
        onMessage(Regex("^.+CASTING IN 3s(.+)?\$"), { alert && enabled }) {
            PlayerUtils.alert("Casting Rag Axe")
        }
        onMessage(Regex("Ragnarock was cancelled due to being hit!"), { alertCancelled && enabled }) {
            PlayerUtils.alert("Rag Axe Cancelled")
        }
    }
}