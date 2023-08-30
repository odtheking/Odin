package me.odinclient.features.impl.skyblock

import me.odinclient.features.Category
import me.odinclient.features.Module
import me.odinclient.utils.skyblock.ChatUtils
import net.minecraft.client.Minecraft
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object AutoRenewCrystalHollows : Module(
    name = "Auto-Renew Hollows Pass",
    category = Category.SKYBLOCK
) {

    init {
        onMessage("Your pass to the Crystal Hollows will expire in 1 minute".toRegex(), { enabled }) {
            ChatUtils.sendCommand("purchasecrystallhollowspass");
        }
    }

}