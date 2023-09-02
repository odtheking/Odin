package me.odinclient.features.impl.skyblock

import me.odinclient.features.Category
import me.odinclient.features.Module
import me.odinclient.utils.skyblock.ChatUtils

object AutoRenewCrystalHollows : Module(
    name = "Auto-Renew Hollows Pass",
    category = Category.SKYBLOCK,
    tag = TagType.NEW,
    description = "Automatically rebuy a crystal hollows pass."
) {

    init {
        onMessage("Your pass to the Crystal Hollows will expire in 1 minute".toRegex(), { enabled }) {
            ChatUtils.sendCommand("purchasecrystallhollowspass");
        }
    }

}