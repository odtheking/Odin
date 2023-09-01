package me.odinclient.features.impl.dungeon

import me.odinclient.events.impl.ChatPacketEvent
import me.odinclient.features.Category
import me.odinclient.features.Module
import me.odinclient.utils.skyblock.ChatUtils
import me.odinclient.utils.skyblock.dungeon.DungeonUtils.inDungeons
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object CustomEnd : Module(
    name = "Auto Extra stats",
    description = "Automatically clicks the Extra Stats at the end of a dungeon.",
    category = Category.DUNGEON,
) {

    @SubscribeEvent
    fun onChat(event: ChatPacketEvent) {
        if (!inDungeons) return
        if (event.message == "                             > EXTRA STATS <") {
            ChatUtils.sendCommand("showextrastats")
        }
    }
}