package me.odinclient.features.impl.dungeon

import me.odinclient.utils.skyblock.PlayerUtils.dropItem
import me.odinmain.events.impl.ChatPacketEvent
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.NumberSetting
import me.odinmain.utils.noControlCodes
import me.odinmain.utils.skyblock.ChatUtils
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import kotlin.math.floor

object AutoWish: Module(
    "Auto Wish",
    category =  Category.DUNGEON,
    description = "Grants wishes for your teammates when they are low on health."
) {
    private val healthPercentage: Double by NumberSetting("Health Percentage", 30.0, 5.0, 80.0, 1.0, description = "The percentage of health to wish at")

    private var canWish = true

    @SubscribeEvent
    fun onChat(event: ChatPacketEvent) {
        val message = event.message.noControlCodes
        if ((message.contains("Wish is ready to use!") || message.contains("Your Healer ULTIMATE wish is now available!")) && !DungeonUtils.inBoss && !DungeonUtils.isGhost)
            canWish = true
        else if (DungeonUtils.inBoss && canWish && !DungeonUtils.isGhost)
            canWish = false
    }

    @SubscribeEvent
    fun onClientTick(event: TickEvent.ClientTickEvent) {
        if (DungeonUtils.inBoss || !DungeonUtils.inDungeons || !canWish) return
        DungeonUtils.teammates.forEach { entityPlayer ->
            val currentHp = entityPlayer.first.health
            if (currentHp < 40 * (healthPercentage / 100) && !DungeonUtils.isGhost) {
                ChatUtils.modMessage("§7${entityPlayer.first.name}§a is at less than §c${floor(healthPercentage)}% §aHP! Wishing!")
                dropItem()
                canWish = false
            }
        }
    }
}