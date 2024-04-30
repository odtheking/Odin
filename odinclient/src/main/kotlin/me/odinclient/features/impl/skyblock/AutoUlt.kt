package me.odinclient.features.impl.skyblock

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.utils.runIn
import me.odinmain.utils.skyblock.PlayerUtils
import me.odinmain.utils.skyblock.modMessage

object AutoUlt : Module(
    "Auto Ult",
    description = "Automatically uses your ult at crucial moments",
    category = Category.SKYBLOCK
) {
    init {
        onMessage("⚠ Maxor is enraged! ⚠", false) {
            PlayerUtils.dropItem()
            modMessage("§cUsing ult!")
        }

        onMessage("[BOSS] Goldor: You have done it, you destroyed the factory…", false) {
            PlayerUtils.dropItem()
            modMessage("§cUsing ult!")
        }

        onMessage("[BOSS] Sadan: My giants! Unleashed!", false) {
            runIn(10) {
                PlayerUtils.dropItem()
                modMessage("§cUsing ult!")
            }
        }
    }
}