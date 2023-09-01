package me.odinclient.features.impl.dungeon

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.odinclient.OdinClient.Companion.scope
import me.odinclient.events.impl.ChatPacketEvent
import me.odinclient.features.Category
import me.odinclient.features.Module
import me.odinclient.features.settings.impl.NumberSetting
import me.odinclient.utils.skyblock.ChatUtils
import me.odinclient.utils.skyblock.dungeon.DungeonUtils
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object AutoDungeonReque : Module(
    name = "Auto Dungeon Requeue",
    description = "Automatically starts a new dungeon at the end of a dungeon.",
    category = Category.DUNGEON,
    tag = TagType.NEW
) {
    private val delay: Int by NumberSetting("Delay", 10, 0, 30, 1)

    @SubscribeEvent
    fun onChat(event: ChatPacketEvent) {
        if (!DungeonUtils.inDungeons || event.message != "                             > EXTRA STATS <") return
        scope.launch {
            delay(delay * 1000L)
            ChatUtils.sendCommand("instancerequeue")
        }
    }
}