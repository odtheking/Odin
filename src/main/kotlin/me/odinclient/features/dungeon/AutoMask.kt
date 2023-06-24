package me.odinclient.features.dungeon

import me.odinclient.OdinClient.Companion.config
import me.odinclient.OdinClient.Companion.mc
import me.odinclient.utils.skyblock.ChatUtils
import me.odinclient.utils.skyblock.ItemUtils
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.client.gui.inventory.GuiInventory
import net.minecraft.util.StringUtils.stripControlCodes
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object AutoMask {

    private var spiritProc = 0L
    private var bonzoProc = 0L
    private const val spiritCooldown = 30000
    private const val bonzoCooldown = 180000

    @SubscribeEvent
    fun onWorldLoad(event: WorldEvent.Load) {
        spiritProc = 0
        bonzoProc = 0
    }

    @SubscribeEvent
    fun onClientChatReceived(event: ClientChatReceivedEvent) {
        if (!config.autoMask) return
        val message = stripControlCodes(event.message.unformattedText)
        val regex = Regex("^(Second Wind Activated!)? ?Your (.+) saved your life!\$")
        when (regex.find(message)?.groupValues?.get(2)) {
            "Spirit Mask" -> spiritProc = System.currentTimeMillis()
            "Bonzo's Mask" -> bonzoProc = System.currentTimeMillis()
            "⚚ Bonzo's Mask" -> bonzoProc = System.currentTimeMillis()
        }
        if (!regex.matches(message)) return

        val inventory = GuiInventory(mc.thePlayer) as GuiContainer

        val currentTime = System.currentTimeMillis()
        if (currentTime - spiritProc >= spiritCooldown) {
            val slotId = ItemUtils.getItemIndexInInventory("Spirit Mask", true)
            if(slotId == -1) return
            mc.playerController.windowClick(inventory.inventorySlots.windowId, slotId, 0, 2, mc.thePlayer)
            mc.playerController.windowClick(inventory.inventorySlots.windowId, 5 , 0, 2, mc.thePlayer)
            mc.playerController.windowClick(inventory.inventorySlots.windowId, slotId, 0, 2, mc.thePlayer)
            ChatUtils.modMessage("Swapped mask!")
        } else if (currentTime - bonzoProc >= bonzoCooldown) {
            val slotId = ItemUtils.getItemIndexInInventory("Bonzo's Mask", true)
            if(slotId == -1 ) return
            mc.playerController.windowClick(inventory.inventorySlots.windowId, slotId, 0, 2, mc.thePlayer)
            mc.playerController.windowClick(inventory.inventorySlots.windowId, 5 , 0, 2, mc.thePlayer)
            mc.playerController.windowClick(inventory.inventorySlots.windowId, slotId, 0, 2, mc.thePlayer)
            ChatUtils.modMessage("Swapped mask!")
        } else ChatUtils.modMessage("Masks are on cooldown or no mask was found!")
    }
}