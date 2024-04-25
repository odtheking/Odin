package me.odinmain.features.impl.skyblock

import me.odinmain.events.impl.PacketSentEvent
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.StringSetting
import me.odinmain.utils.skyblock.modMessage
import net.minecraft.inventory.ContainerRepair
import net.minecraft.item.Item
import net.minecraft.network.play.client.C0EPacketClickWindow
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object PartyNote : Module(
    name = "Party Note",
    category = Category.SKYBLOCK,
    description = "Allows you to enter a custom party finder note with color codes (&)"
) {

    private val partyNote: String by StringSetting(name = "Note")

    @SubscribeEvent
    fun onPacketSend(event: PacketSentEvent) {
        val packet = event.packet
        if (packet !is C0EPacketClickWindow) return
        if (mc.thePlayer.openContainer !is ContainerRepair || packet.slotId != 2 || Item.getIdFromItem(packet.clickedItem.item) != 339) return
        val itemName = partyNote
            .replace("&", "§")
            .replace("§§", "&")
            .replace(Regex("§([0-9a-fklmnor])")) { "§§${it.groupValues[1]}${it.groupValues[1]}" }
        packet.clickedItem.setStackDisplayName(itemName)
        modMessage(itemName)
        modMessage(packet.clickedItem?.displayName ?: "null")
    }

}