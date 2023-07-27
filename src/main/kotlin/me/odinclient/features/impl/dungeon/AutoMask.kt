package me.odinclient.features.impl.dungeon

import me.odinclient.OdinClient.Companion.mc
import me.odinclient.features.Category
import me.odinclient.features.Module
import me.odinclient.utils.Utils.noControlCodes
import me.odinclient.utils.clock.Clock
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
    private val spiritClock = Clock(30_000)
    private val bonzoClock = Clock(180_000)

    @SubscribeEvent
    fun onClientChatReceived(event: ClientChatReceivedEvent) {
        val msg = event.message.unformattedText.noControlCodes
        val regex = Regex("^(Second Wind Activated!)? ?Your (.+) saved your life!\$")
        if (!regex.matches(msg)) return

        when (regex.find(msg)?.groupValues?.get(2)) {
            "Spirit Mask" -> spiritClock.update()
            "Bonzo's Mask", "⚚ Bonzo's Mask" -> bonzoClock.update()
        }

        val inventory = GuiInventory(mc.thePlayer) as GuiContainer

        if (spiritClock.hasTimePassed()) {

            val slotId = ItemUtils.getItemSlot("Spirit Mask", true) ?: return
            val windowID = inventory.inventorySlots.windowId

            windowClick(windowID, slotId, 0, 2)
            windowClick(windowID, 5, 0, 2)
            windowClick(windowID, slotId, 0, 2)

            ChatUtils.modMessage("Swapped mask!")
        } else if (bonzoClock.hasTimePassed()) {

            val slotId = ItemUtils.getItemSlot("Bonzo's Mask", true) ?: return
            val windowID = inventory.inventorySlots.windowId

            windowClick(windowID, slotId, 0, 2)
            windowClick(windowID, 5, 0, 2)
            windowClick(windowID, slotId, 0, 2)

            ChatUtils.modMessage("Swapped mask!")
        } else ChatUtils.modMessage("Masks are on cooldown or no mask was found!")
    }

    @SubscribeEvent
    fun onWorldLoad(event: WorldEvent.Load) {
        spiritClock.setTime(0)
        bonzoClock.setTime(0)
    }
}