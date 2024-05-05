package me.odinmain.features.impl.skyblock

import me.odinmain.OdinMain.mc
import me.odinmain.events.impl.PacketSentEvent
import net.minecraft.client.gui.GuiRepair
import net.minecraft.inventory.ContainerRepair
import net.minecraft.item.Item
import net.minecraft.network.play.client.C0EPacketClickWindow
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object PartyNote {

    @SubscribeEvent
    fun onPacketSend(event: PacketSentEvent) {
        val packet = event.packet
        if (packet !is C0EPacketClickWindow) return
        if (mc.thePlayer.openContainer !is ContainerRepair || mc.currentScreen !is GuiRepair || packet.slotId != 2 || Item.getIdFromItem(packet.clickedItem.item) != 339) return
        val itemName = packet.clickedItem.displayName
            .replace("&", "§")
            .replace("§§", "&")
            .replace(Regex("§([0-9a-fklmnor])")) { "§§${it.groupValues[1]}${it.groupValues[1]}" }
        packet.clickedItem.setStackDisplayName(itemName)
    }
}