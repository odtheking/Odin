package me.odinclient.features.impl.dungeon

import me.odinclient.features.Category
import me.odinclient.features.Module
import me.odinclient.utils.skyblock.ChatUtils
import me.odinclient.utils.skyblock.ChatUtils.unformattedText
import me.odinclient.utils.skyblock.dungeon.DungeonUtils.inDungeons
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object CustomEnd : Module(
    name = "Auto Extra stats",
    description = "Automatically clicks the Extra Stats at the end of a dungeon.",
    category = Category.DUNGEON
) {
    // TODO: Test
    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    fun onChat(event: ClientChatReceivedEvent) {
        if (!inDungeons || event.type.toInt() == 2) return

        if (event.unformattedText == "                             > EXTRA STATS <") {
            ChatUtils.sendCommand("showextrastats")
        }
    }
}