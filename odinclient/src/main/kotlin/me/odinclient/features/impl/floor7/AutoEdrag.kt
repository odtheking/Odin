package me.odinclient.features.impl.floor7

import me.odinclient.utils.skyblock.PlayerUtils
import me.odinmain.events.impl.ChatPacketEvent
import me.odinmain.events.impl.GuiLoadedEvent
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.utils.skyblock.sendCommand
import me.odinmain.utils.skyblock.getItemIndexInContainerChest
import me.odinmain.utils.skyblock.modMessage
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object AutoEdrag: Module(
    "Auto Ender Dragon",
    description = "Automatically clicks the Ender Dragon pet at the start of p5.",
    category = Category.FLOOR7,
) {
    private var going = false

    @SubscribeEvent
    fun onChat(event: ChatPacketEvent) {
        if (event.message == "[BOSS] Wither King: You.. again?") {
            sendCommand("pets")
            going = true
        }
    }

    @SubscribeEvent
    fun guiOpen(event: GuiLoadedEvent) {
        if (!going || event.name != "Pets") return
        val index = getItemIndexInContainerChest(event.gui, "Ender Dragon", true)
            ?: return modMessage("§cCouldn't find §fEnder Dragon!")
        PlayerUtils.windowClick(index, 2, 3)
    }
}