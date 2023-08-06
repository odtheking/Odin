package me.odinclient.features.impl.dungeon

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.odinclient.OdinClient.Companion.mc
import me.odinclient.OdinClient.Companion.scope
import me.odinclient.events.ChatPacketEvent
import me.odinclient.features.Category
import me.odinclient.features.Module
import me.odinclient.utils.Utils.noControlCodes
import me.odinclient.utils.skyblock.ChatUtils
import me.odinclient.utils.skyblock.dungeon.DungeonUtils
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent

object CustomEnd : Module(
    name = "Custom End Info",
    description = "Shows run info in your chat at the end of the run",
    category = Category.DUNGEON
) {
    private var hasShownExtraStats = false
    private val msgList = listOf("Master Mode Catacombs - ", "The Catacombs - ")

    @SubscribeEvent
    fun onWorldUnload(event: WorldEvent.Unload) {
        hasShownExtraStats = false
    }

    @SubscribeEvent(receiveCanceled = true)
    fun onChatReceived(event: ChatPacketEvent) {
        if (!DungeonUtils.inDungeons || hasShownExtraStats) return
        val stripped = event.message.trim().replace(",", "")
        if (msgList.any { stripped.startsWith(it) }) {
            scope.launch {
                delay(3000)
                ChatUtils.sendCommand("showextrastats")
            }
            hasShownExtraStats = true
        }
    }

}