package me.odinclient.features.impl.floor7

import me.odinclient.events.impl.ChatPacketEvent
import me.odinclient.features.Category
import me.odinclient.features.Module
import me.odinclient.features.settings.impl.SelectorSetting
import me.odinclient.utils.skyblock.ChatUtils
import me.odinclient.utils.skyblock.dungeon.DungeonUtils
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object RelicAnnouncer : Module(
    name = "Relic Announcer",
    description = "Automatically announce your relic to the rest of the party",
    category = Category.FLOOR7
) {
    private val colors = arrayListOf("Green", "Purple", "Blue", "Orange", "Red")
    private val selected: Int by SelectorSetting("Color", "Green", colors)

    @SubscribeEvent
    fun onChatReceived(event: ChatPacketEvent) {
        if (!DungeonUtils.inDungeons) return
        if (event.message !== "[BOSS] Necron: All this, for nothing...") return
        ChatUtils.partyMessage("${colors[selected]} Relic")
    }
}