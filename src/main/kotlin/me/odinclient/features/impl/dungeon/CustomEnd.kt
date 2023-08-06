package me.odinclient.features.impl.dungeon

import me.odinclient.OdinClient.Companion.mc
import me.odinclient.features.Category
import me.odinclient.features.Module
import me.odinclient.utils.Utils.noControlCodes
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


    private var ticks = 0
    private var showExtraStatsTime = -1
    private var hasShownExtraStats = false

    @SubscribeEvent
    fun onWorldUnload(event: WorldEvent.Unload) {
        hasShownExtraStats = false
    }

    @SubscribeEvent(receiveCanceled = true)
    fun onChatReceived(event: ClientChatReceivedEvent) {
        if (!DungeonUtils.inDungeons) return
        val stripped = event.message.unformattedText.noControlCodes.trim().replace(",", "")
        if (!hasShownExtraStats && listOf("Master Mode Catacombs - ", "The Catacombs - ").any { stripped.startsWith(it) }) {
            showExtraStatsTime = ticks + 10
            hasShownExtraStats = true
        }
    }

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (event.phase != TickEvent.Phase.START) return
        if (ticks == showExtraStatsTime) {
            mc.thePlayer?.sendChatMessage("/showextrastats")
        }
        ticks++
    }
}