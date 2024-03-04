package me.odinmain.features.impl.skyblock

import me.odinmain.OdinMain
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.NumberSetting
import me.odinmain.utils.skyblock.PlayerUtils
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.skyblock.modMessage
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent

object Reminders : Module(
    "Reminders",
    description = "Helpful reminders for dungeons.",
    category = Category.SKYBLOCK
) {
    private val ultReminder: Boolean by BooleanSetting("Ult Reminder", description = "Reminds you to use your ult.")
    private val autoUlt: Boolean by BooleanSetting("Auto Ult", false, description = "Automatically uses your ult at crucial moments.").withDependency { !OdinMain.onLegitVersion }
    private val dragReminder: Boolean by BooleanSetting("Drag Reminder", description = "Reminds you to swap to edrag.")
    private val maskAlert: Boolean by BooleanSetting("Mask Alert", description = "Alerts you when your mask is used.")
    private val wishAlert: Boolean by BooleanSetting("Wish Alert", description = "Alerts you when teammates are low on health.")
    private val autoWish: Boolean by BooleanSetting("Auto Wish", false, description = "Automatically wishes for teammates when they are low on health.").withDependency { !OdinMain.onLegitVersion }
    private val healthPercentage: Int by NumberSetting("Health Percentage", 40, 0, 80, 1)

    private var canWish = true

    init {
        onMessage("[BOSS] Wither King: You.. again?", dragReminder) {
            PlayerUtils.alert("§3Swap to edrag!")
        }

        onMessage("⚠ Maxor is enraged! ⚠", false) {
            if (ultReminder) PlayerUtils.alert("§3Use ult!")
            if (autoUlt && !OdinMain.onLegitVersion) PlayerUtils.dropItem()
        }

        onMessage("[BOSS] Goldor: You have done it, you destroyed the factory…", false) {
            if (ultReminder) PlayerUtils.alert("§3Use ult!")
            if (autoUlt && !OdinMain.onLegitVersion) PlayerUtils.dropItem()
        }

        onMessage("[BOSS] Sadan: My giants! Unleashed!", false) {
            if (ultReminder) PlayerUtils.alert("§3Use ult")
            if (autoUlt && !OdinMain.onLegitVersion) PlayerUtils.dropItem()
        }

        onMessage("Wish is ready to use!", false) {
            if (!DungeonUtils.inBoss && !DungeonUtils.isGhost) canWish = true else if (DungeonUtils.inBoss && canWish && !DungeonUtils.isGhost) canWish = false
        }

        onMessage("Your Healer ULTIMATE wish is now available!", false) {
            if (!DungeonUtils.inBoss && !DungeonUtils.isGhost) canWish = true else if (DungeonUtils.inBoss && canWish && !DungeonUtils.isGhost) canWish = false
        }

        onMessage("^(Second Wind Activated!)? ?Your (.+) saved your life!\$", maskAlert) {
            PlayerUtils.alert("Mask used!")
            modMessage("Mask used!")
        }
    }

    @SubscribeEvent
    fun onClientTick(event: TickEvent.ClientTickEvent) {
        if (DungeonUtils.inBoss || !DungeonUtils.inDungeons || !canWish) return
        DungeonUtils.teammates.forEach { entityPlayer ->
            val currentHp = entityPlayer.entity?.health ?: 40f
            if (currentHp < 40 * (healthPercentage / 100) && !DungeonUtils.isGhost) {
                if (wishAlert) {
                    modMessage("§7${entityPlayer.name}§a is at less than §c$healthPercentage% §aHP!")
                    PlayerUtils.alert("USE WISH")
                }
                if (autoWish && !OdinMain.onLegitVersion) {
                    PlayerUtils.dropItem()
                    canWish = false
                }
            }
        }
    }
}