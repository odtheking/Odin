package me.odinmain.features.impl.floor7

import me.odinmain.events.impl.ChatPacketEvent
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.SelectorSetting
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.skyblock.partyMessage
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object RelicAnnouncer : Module(
    name = "Relic Announcer",
    description = "Automatically announce your relic to the rest of the party.",
    category = Category.FLOOR7
) {
    private val colors = arrayListOf("Green", "Purple", "Blue", "Orange", "Red")
    private val selected: Int by SelectorSetting("Color", "Green", colors, description = "The color of your relic.")

    @SubscribeEvent
    fun onChatReceived(event: ChatPacketEvent) {
        if (!DungeonUtils.inDungeons) return
        if (event.message !== "[BOSS] Necron: All this, for nothing...") return
        partyMessage("${colors[selected]} Relic")
    }
}