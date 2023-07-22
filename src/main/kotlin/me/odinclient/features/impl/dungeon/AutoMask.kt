package me.odinclient.features.impl.dungeon

import me.odinclient.OdinClient.Companion.mc
import me.odinclient.features.Category
import me.odinclient.features.Module
import me.odinclient.utils.Utils.noControlCodes
import me.odinclient.utils.skyblock.ChatUtils
import me.odinclient.utils.skyblock.ItemUtils
import me.odinclient.utils.skyblock.PlayerUtils.windowClick
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.client.gui.inventory.GuiInventory
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object AutoMask : Module(
    name = "Auto Mask",
    description = "Automatically uses masks when they proc",
    category = Category.DUNGEON
) {

    private var spiritProc = 0L
    private var bonzoProc = 0L
    private const val spiritCooldown = 30000L
    private const val bonzoCooldown = 180000L

    @SubscribeEvent
    fun onWorldLoad(event: WorldEvent.Load) {
        spiritProc = 0
        bonzoProc = 0
    }

    @SubscribeEvent
    fun onClientChatReceived(event: ClientChatReceivedEvent) {
        val msg = event.message.unformattedText.noControlCodes
        val regex = Regex("^(Second Wind Activated!)? ?Your (.+) saved your life!\$")
        if (!regex.matches(msg)) return

        when (regex.find(msg)?.groupValues?.get(2)) {
            "Spirit Mask" -> spiritProc = System.currentTimeMillis()
            "Bonzo's Mask", "⚚ Bonzo's Mask" -> bonzoProc = System.currentTimeMillis()
        }

        val inventory = GuiInventory(mc.thePlayer) as GuiContainer
        val currentTime = System.currentTimeMillis()
        if (currentTime - spiritProc >= spiritCooldown) {

            val slotId = ItemUtils.getItemSlot("Spirit Mask", true) ?: return
            val windowID = inventory.inventorySlots.windowId

            windowClick(windowID, slotId, 0, 2)
            windowClick(windowID, 5, 0, 2)
            windowClick(windowID, slotId, 0, 2)

            ChatUtils.modMessage("Swapped mask!")
        } else if (currentTime - bonzoProc >= bonzoCooldown) {

            val slotId = ItemUtils.getItemSlot("Bonzo's Mask", true) ?: return
            val windowID = inventory.inventorySlots.windowId

            windowClick(windowID, slotId, 0, 2)
            windowClick(windowID, 5, 0, 2)
            windowClick(windowID, slotId, 0, 2)

            ChatUtils.modMessage("Swapped mask!")
        } else ChatUtils.modMessage("Masks are on cooldown or no mask was found!")
    }
}