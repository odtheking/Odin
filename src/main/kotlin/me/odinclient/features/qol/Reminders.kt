package me.odinclient.features.qol

import me.odinclient.OdinClient.Companion.config
import me.odinclient.OdinClient.Companion.mc
import me.odinclient.utils.skyblock.ChatUtils
import me.odinclient.utils.skyblock.PlayerUtils
import me.odinclient.utils.skyblock.dungeon.DungeonUtils
import net.minecraft.util.StringUtils
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent

object Reminders {

    private var firstlaser = false
    private var timeindungeonm: Long = 0
    private var notified = false
    private var playerready = false

    private val alertMap = mapOf(
        "[BOSS] Wither King: You.. again?" to "&3Swap to edrag!",
        "[BOSS] Maxor: YOU TRICKED ME!" to "&3Use ult!",
        "[BOSS] Maxor: THAT BEAM! IT HURTS! IT HURTS!!" to "&3Use ult!",
        "[BOSS] Goldor: You have done it, you destroyed the factory…" to "&3Use ult!",
        "[BOSS] Sadan: My giants! Unleashed!" to "&3Use ult!"
        // Add more pairs here as needed
    )

    @SubscribeEvent
    fun onClientChatReceived(event: ClientChatReceivedEvent) {
        if (!config.autoLeap || !DungeonUtils.inDungeons) return

        val message = StringUtils.stripControlCodes(event.message.unformattedText)

        if (message in alertMap) {
            if (message.startsWith("[BOSS] Maxor:") && firstlaser) return
            if(message.startsWith("[BOSS] Wither King") && !config.dragonReminder) return
            if(message.startsWith("[BOSS] Maxor") && !config.ultReminder) return
            if(message.startsWith("[BOSS] Sadan") && !config.ultReminder) return
            if(message.startsWith("[BOSS] Goldor") && !config.ultReminder) return

            val alert = alertMap[message]
            PlayerUtils.alert(alert!!)
            ChatUtils.modMessage(alert)

            if (message.startsWith("[BOSS] Maxor:")) {
                firstlaser = true
            }
        }
    }

    @SubscribeEvent
    fun onWorldLoad(event: WorldEvent.Load) {
        timeindungeonm = System.currentTimeMillis()
        notified = false
        playerready = false
        firstlaser = false
    }

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (!config.readyReminder || !DungeonUtils.inDungeons || playerready || notified || System.currentTimeMillis() - timeindungeonm <= 7000) return
        PlayerUtils.alert("&3Ready up!")
        ChatUtils.modMessage("Ready up!")
        notified = true
    }

    @SubscribeEvent
    fun playerReady(event: ClientChatReceivedEvent) {
        val message = event.message.unformattedText
        if (message == "${mc.thePlayer.name} is now ready!") {
            playerready = true
            mc.thePlayer.closeScreen()
        }
    }
}