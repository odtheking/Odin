package me.odinmain.features.impl.skyblock

import me.odinmain.OdinMain
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.utils.skyblock.PlayerUtils

object Reminders : Module(
    "Reminders",
    description = "Helpful reminders for dungeons.",
    category = Category.SKYBLOCK
) {
    private val ultReminder: Boolean by BooleanSetting("Ult Reminder", description = "Reminds you to use your ult.")
    private val autoUlt: Boolean by BooleanSetting("Auto Ult", false, description = "Automatically uses your ult at crucial moments.").withDependency { !OdinMain.isLegitVersion }
    private val dragReminder: Boolean by BooleanSetting("Drag Reminder", description = "Reminds you to swap to edrag.")

    init {
        onMessage("[BOSS] Wither King: You.. again?", false, { dragReminder && enabled }) {
            PlayerUtils.alert("§3Swap to edrag!")
        }

        onMessage("⚠ Maxor is enraged! ⚠", false) {
            if (ultReminder) PlayerUtils.alert("§3Use ult!")
            if (autoUlt && !OdinMain.isLegitVersion) PlayerUtils.dropItem()
        }

        onMessage("[BOSS] Goldor: You have done it, you destroyed the factory…", false) {
            if (ultReminder) PlayerUtils.alert("§3Use ult!")
            if (autoUlt && !OdinMain.isLegitVersion) PlayerUtils.dropItem()
        }

        onMessage("[BOSS] Sadan: My giants! Unleashed!", false) {
            if (ultReminder) PlayerUtils.alert("§3Use ult")
            if (autoUlt && !OdinMain.isLegitVersion) PlayerUtils.dropItem()
        }
    }
}