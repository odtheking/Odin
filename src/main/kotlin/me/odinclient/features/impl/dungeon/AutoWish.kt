package me.odinclient.features.impl.dungeon

import me.odinclient.features.Category
import me.odinclient.features.Module
import me.odinclient.features.settings.impl.NumberSetting
import me.odinclient.utils.Utils.noControlCodes
import me.odinclient.utils.skyblock.ChatUtils
import me.odinclient.utils.skyblock.PlayerUtils
import me.odinclient.utils.skyblock.dungeon.DungeonUtils
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.lwjgl.input.Keyboard
import kotlin.math.floor

object AutoWish: Module(
    "Auto Wish",
    Keyboard.KEY_NONE,
    Category.DUNGEON
) {

    private val healthPercentage: Double by NumberSetting("Health Percentage", 30.0, 5.0, 80.0, 1.0, description = "The percentage of health to wish at")

    private var canWish = true

    @SubscribeEvent
    fun onChat(event: ClientChatReceivedEvent) {
        val message = event.message.unformattedText.noControlCodes
        if ((message.contains("Wish is ready to use!") || message.contains("Your Healer ULTIMATE wish is now available!")) && !DungeonUtils.inBoss && !DungeonUtils.isGhost)
            canWish = true
        else if (DungeonUtils.inBoss && canWish && !DungeonUtils.isGhost)
            canWish = false
    }

    @SubscribeEvent
    fun onClientTick(event: TickEvent.ClientTickEvent) {
        if (!this.enabled || DungeonUtils.inBoss || !DungeonUtils.inDungeons || !canWish) return
        DungeonUtils.teammates.forEach { entityPlayer ->
            val currentHp = entityPlayer.first.health
            if (currentHp < 40 * (healthPercentage / 100) && !DungeonUtils.isGhost) {
                ChatUtils.modMessage("§7${entityPlayer.first.name}§a is at less than §c${floor(healthPercentage)}% §aHP! Wishing!")
                PlayerUtils.dropItem()
                canWish = false
            }
        }
    }
}