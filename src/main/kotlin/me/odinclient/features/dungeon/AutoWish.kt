package me.odinclient.features.dungeon

import me.odinclient.OdinClient.Companion.config
import me.odinclient.utils.Utils.noControlCodes
import me.odinclient.utils.skyblock.ChatUtils
import me.odinclient.utils.skyblock.PlayerUtils
import me.odinclient.utils.skyblock.dungeon.DungeonUtils
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import kotlin.math.floor


object AutoWish {

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
        if (!config.autoWish || DungeonUtils.inBoss || !DungeonUtils.inDungeons || !canWish) return
        DungeonUtils.teammates.forEach { entityPlayer ->
            val currentHp = entityPlayer.first.health
            val healthPercent = 40 * (config.healthPrecentage / 100)
            if (currentHp < 40 * (config.healthPrecentage / 100) && !DungeonUtils.isGhost) {
                ChatUtils.modMessage("§7${entityPlayer.first.name}§a is at less than §c${floor(config.healthPrecentage)}% §aHP! Wishing!")
                PlayerUtils.dropItem()
                canWish = false
            }
        }
    }
}