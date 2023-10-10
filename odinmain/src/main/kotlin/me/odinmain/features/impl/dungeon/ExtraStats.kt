package me.odinmain.features.impl.dungeon

import me.odinmain.events.impl.ChatPacketEvent
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.utils.skyblock.ChatUtils
import me.odinmain.utils.skyblock.dungeon.DungeonUtils.inDungeons
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object ExtraStats : Module(
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
