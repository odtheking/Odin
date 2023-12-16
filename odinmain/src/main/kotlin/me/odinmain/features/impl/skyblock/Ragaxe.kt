package me.odinmain.features.impl.skyblock

import me.odinmain.events.impl.ChatPacketEvent
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.utils.skyblock.PlayerUtils
import me.odinmain.utils.skyblock.heldItem
import me.odinmain.utils.skyblock.itemID
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object Ragaxe : Module(
    name = "Rag Axe",
    description = "Tracks rag axe cooldowns.",
    category = Category.SKYBLOCK
) {
    @SubscribeEvent
    fun onChat(event: ChatPacketEvent) {

        if (event.message.contains("CASTING") && heldItem.itemID == "Ragnarock Axe")
            PlayerUtils.alert("Casting Rag Axe")


        if(event.message == "Ragnarock was cancelled due to being hit!")
            PlayerUtils.alert("Rag Axe Cancelled")

    }


}