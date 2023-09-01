package me.odinclient.features.impl.dungeon

import me.odinclient.events.impl.ChatPacketEvent
import me.odinclient.features.Category
import me.odinclient.features.Module
import me.odinclient.utils.skyblock.ChatUtils
import me.odinclient.utils.skyblock.dungeon.DungeonUtils
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object AutoDungeonReque : Module(
    name = "Auto Dungeon Requeue",
    description = "Automatically starts a new dungeon at the end of a dungeon.",
    category = Category.DUNGEON,
    tag = TagType.NEW
) {

    @SubscribeEvent
    fun onChat(event: ChatPacketEvent) {
        if (!DungeonUtils.inDungeons) return
        if (event.message == "                             > EXTRA STATS <") {
            ChatUtils.sendCommand("instancerequeue")
        }
    }
}