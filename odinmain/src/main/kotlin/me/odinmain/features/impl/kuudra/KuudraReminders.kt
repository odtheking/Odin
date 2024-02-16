package me.odinmain.features.impl.kuudra

import me.odinmain.events.impl.ChatPacketEvent
import me.odinmain.features.Category
import me.odinmain.features.Module
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object KuudraReminders : Module(
    name = "Kuudra Reminders",
    description = "Displays kuudra information in Kuudra.",
    category = Category.KUUDRA
) {
    @SubscribeEvent
    fun onChat(event: ChatPacketEvent) {

    }
}