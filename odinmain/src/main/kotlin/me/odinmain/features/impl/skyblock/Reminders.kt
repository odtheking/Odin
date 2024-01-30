package me.odinmain.features.impl.skyblock

import me.odinmain.events.impl.ChatPacketEvent
import me.odinmain.features.Category
import me.odinmain.features.Module
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
    private val ultReminder: Boolean by BooleanSetting("Ult Reminder")
    private val dragReminder: Boolean by BooleanSetting("Drag Reminder")
    private val maskAlert: Boolean by BooleanSetting("Mask Alert")
    private val wishAlert: Boolean by BooleanSetting("Wish Alert")
    private val healthPercentage: Int by NumberSetting("Health Percentage", 40, 0, 80, 1)

    private var canWish = true

    @SubscribeEvent
    fun onChat(event: ChatPacketEvent) {

        when (event.message) {
            "[BOSS] Wither King: You.. again?" -> if (!dragReminder) return else PlayerUtils.alert("§3Swap to edrag!")

            "⚠ Maxor is enraged! ⚠" -> PlayerUtils.alert("§3Use ult!")

            "[BOSS] Goldor: You have done it, you destroyed the factory…" -> if (!ultReminder) return else PlayerUtils.alert("§3Use ult!")

            "[BOSS] Sadan: My giants! Unleashed!" -> if (!ultReminder) return else PlayerUtils.alert("§3Use ult!")

            "Wish is ready to use!" -> {
                if (!DungeonUtils.inBoss && !DungeonUtils.isGhost) canWish = true else if (DungeonUtils.inBoss && canWish && !DungeonUtils.isGhost) canWish = false
            }

            "Your Healer ULTIMATE wish is now available!" -> {
                if (!DungeonUtils.inBoss && !DungeonUtils.isGhost) canWish = true else if (DungeonUtils.inBoss && canWish && !DungeonUtils.isGhost) canWish = false
            }

            else -> {
                if (maskAlert && Regex("^(Second Wind Activated!)? ?Your (.+) saved your life!\$").matches(event.message)) {
                    PlayerUtils.alert("Mask used!")
                    modMessage("Mask used!")
                }
            }
        }
    }

    @SubscribeEvent
    fun onClientTick(event: TickEvent.ClientTickEvent) {
        if (!wishAlert || DungeonUtils.inBoss || !DungeonUtils.inDungeons || !canWish) return
        DungeonUtils.teammates.forEach { entityPlayer ->
            val currentHp = entityPlayer.entity?.health ?: 40f
            if (currentHp < 40 * (healthPercentage / 100) && !DungeonUtils.isGhost) {
                modMessage("§7${entityPlayer.name}§a is at less than §c$healthPercentage% §aHP!")
                PlayerUtils.alert("USE WISH")
                canWish = false
            }
        }
    }
}