package me.odinmain.features.impl.skyblock

import me.odinmain.events.impl.ChatPacketEvent
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.utils.skyblock.PlayerUtils
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object Ragaxe : Module(
    name = "Rag Axe",
    description = "Tracks rag axe cooldowns.",
    category = Category.SKYBLOCK
) {
    private val alert: Boolean by BooleanSetting("Alert", true, description = "Alerts you when you start casting rag axe.")
    private val alertCancelled: Boolean by BooleanSetting("Alert Cancelled", true, description = "Alerts you when your rag axe is cancelled.")
    @SubscribeEvent
    fun onChat(event: ChatPacketEvent) {

        if (event.message.endsWith("CASTING") && alert)
            PlayerUtils.alert("Casting Rag Axe")

        if(event.message == "Ragnarock was cancelled due to being hit!" && alertCancelled)
            PlayerUtils.alert("Rag Axe Cancelled")
    }
}