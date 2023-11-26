package me.odinmain.features.impl.dungeon

import me.odinmain.events.impl.ChatPacketEvent
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.utils.skyblock.dungeon.DungeonUtils.inDungeons
import me.odinmain.utils.skyblock.sendCommand
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object ExtraStats : Module(
    name = "Auto Extra Stats",
    description = "Automatically clicks the Extra Stats at the end of a dungeon.",
    category = Category.DUNGEON,
) {

    @SubscribeEvent
    fun onChat(event: ChatPacketEvent) {
        if (!inDungeons) return
        if (event.message == "                             > EXTRA STATS <") {
            sendCommand("showextrastats")
        }
    }
}
