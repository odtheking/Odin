package me.odinmain.features.impl.skyblock

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.utils.skyblock.sendCommand

object AutoRenewCrystalHollows : Module(
    name = "Hollows Pass Renew",
    category = Category.SKYBLOCK,
    description = "Automatically rebuy a crystal hollows pass."
) {

    init {
        onMessage("Your pass to the Crystal Hollows will expire in 1 minute".toRegex(), { enabled }) {
            sendCommand("purchasecrystallhollowspass");
        }
    }

}