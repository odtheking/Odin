package me.odinclient.features.impl.dungeon

import me.odinclient.utils.skyblock.PlayerUtils
import me.odinmain.events.impl.ChatPacketEvent
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.SelectorSetting
import me.odinmain.utils.noControlCodes
import me.odinmain.utils.skyblock.modMessage
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object AutoUlt : Module(
    "Auto Ultimate",
    category = Category.DUNGEON,
    description = "Activates your ult at crucial moments, such as when giants spawn, Maxor is stuck in the first laser, or Goldor starts."
) {
    private val mode: Int by SelectorSetting("Mode", "Legit", arrayListOf("Legit", "Auto"))

    private var firstLaser = false

    @SubscribeEvent
    fun onWorldLoad(event: WorldEvent.Load) {
        firstLaser = false
    }

    @SubscribeEvent
    fun onChat(event: ChatPacketEvent) {
        when (event.message.noControlCodes) {
            "[BOSS] Maxor: YOU TRICKED ME!", "[BOSS] Maxor: THAT BEAM! IT HURTS! IT HURTS!!" -> {
                if (firstLaser) return

                if (mode == 0) {
                    modMessage("§3Use ult!")
                    PlayerUtils.alert("§3Use ult!")
                    return
                }

                modMessage("§eFrenzy soon... ULT TIME!")
                PlayerUtils.dropItem()
                firstLaser = true
            }

            "[BOSS] Goldor: You have done it, you destroyed the factory…" -> {
                if (mode == 0) {
                    modMessage("§3Use ult!")
                    PlayerUtils.alert("§3Use ult!")
                    return
                }
                modMessage("§eGoldor time zzz")
                PlayerUtils.dropItem()
            }

            "[BOSS] Sadan: My giants! Unleashed!" -> {
                if (mode == 0) {
                    modMessage("§3Use ult!")
                    PlayerUtils.alert("§3Use ult!")
                    return
                }
                execute(3000, 0) {
                    modMessage("§eGiants incoming")
                    PlayerUtils.dropItem()
                }
            }
        }
    }
}