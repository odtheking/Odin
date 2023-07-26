package me.odinclient.features.impl.dungeon

import me.odinclient.features.Category
import me.odinclient.features.Module
import me.odinclient.utils.skyblock.ChatUtils
import me.odinclient.utils.skyblock.ChatUtils.unformattedText
import me.odinclient.utils.skyblock.PlayerUtils
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object AutoUlt : Module(
    "Auto Ultimate",
    category = Category.DUNGEON
) {

    private var firstLaser = false

    @SubscribeEvent
    fun onWorldLoad(event: WorldEvent.Load) {
        firstLaser = false
    }

    @SubscribeEvent
    fun onChat(event: ClientChatReceivedEvent) {
        when (event.unformattedText) {
            "[BOSS] Maxor: YOU TRICKED ME!", "[BOSS] Maxor: THAT BEAM! IT HURTS! IT HURTS!!" -> {

                if (firstLaser) return
                ChatUtils.modMessage("§eFrenzy soon... ULT TIME!")
                PlayerUtils.dropItem()
                firstLaser = true
            }

            "[BOSS] Goldor: You have done it, you destroyed the factory…" -> {
                ChatUtils.modMessage("§eGoldor time zzz")
                PlayerUtils.dropItem()
            }

            "[BOSS] Sadan: My giants! Unleashed!" -> {
                execute(3000, 0) {
                    ChatUtils.modMessage("§eGiants incoming")
                    PlayerUtils.dropItem()
                }
            }
        }
    }
}