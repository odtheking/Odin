package me.odinclient.features.impl.qol

import me.odinclient.OdinClient.Companion.config
import me.odinclient.OdinClient.Companion.mc
import me.odinclient.utils.skyblock.ChatUtils
import me.odinclient.utils.skyblock.PlayerUtils
import net.minecraft.client.settings.KeyBinding.setKeyBindState
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent

object ItemKeybinds {
    private var aotsSwitch = false
    private var precorsorSwitch = false
    private var veilSwitch = false
    private var wandSwitch = false
    private var lastAotsSwitch = 0L
    private var lastPrecorsorSwitch = 0L
    private var lastVeilSwitch = 0L
    private var lastWandSwitch = 0L
    private var lastIceSpray = 0L

    @SubscribeEvent
    fun onTick(event: ClientTickEvent) {
        if (mc.currentScreen == null) return
        if (config.iceSprayKeybind.isActive && System.currentTimeMillis() - lastIceSpray > 1000) {
            lastIceSpray = System.currentTimeMillis()
            ChatUtils.modMessage("Used Ice Spray Wand")
            PlayerUtils.useItem("Ice Spray Wand")
        } else if (config.aotsKeybind.isActive && System.currentTimeMillis() - lastAotsSwitch > 1000) {
            lastAotsSwitch = System.currentTimeMillis()
            aotsSwitch = !aotsSwitch
            ChatUtils.modMessage("Aots is now $aotsSwitch")
        } else if (config.precursorKeybind.isActive && System.currentTimeMillis() - lastPrecorsorSwitch > 1000) {
            lastPrecorsorSwitch = System.currentTimeMillis()
            precorsorSwitch = !precorsorSwitch
            ChatUtils.modMessage("Precorsor is now $precorsorSwitch")
        } else if (config.fireVeilKeybind.isActive && System.currentTimeMillis() - lastVeilSwitch > 1000) {
            lastVeilSwitch = System.currentTimeMillis()
            veilSwitch = !veilSwitch
            ChatUtils.modMessage("Fire Veil is now ${veilSwitch}Switch")
        } else if (config.wandOfAtonementKeybind.isActive && System.currentTimeMillis() - lastWandSwitch > 1000) {
            lastWandSwitch = System.currentTimeMillis()
            wandSwitch = !wandSwitch
            ChatUtils.modMessage("Wand is now $wandSwitch")
        }
    }

    private var lastAots = 0L
    private var lastVeil = 0L
    private var lastWand = 0L
    private var lastPrecursor = 0L
    @SubscribeEvent
    fun onTick2(event: ClientTickEvent) {
        if (event.phase != TickEvent.Phase.END) return
        val currentMillis = System.currentTimeMillis()

        if (veilSwitch && currentMillis - lastVeil > 4500) {
            lastVeil = currentMillis
            PlayerUtils.useItem("Fire Veil Wand")
        } else if (wandSwitch && currentMillis - lastWand > 6000) {
            lastWand = currentMillis
            PlayerUtils.useItem("Wand of Atonement")
        } else if (aotsSwitch && currentMillis - lastAots > 250) {
            lastAots = currentMillis
            PlayerUtils.useItem("Axe of the Shredded")
        } else if (precorsorSwitch && currentMillis - lastPrecursor > 100) {
            setKeyBindState(mc.gameSettings.keyBindSneak.keyCode, true)
            setKeyBindState(mc.gameSettings.keyBindSneak.keyCode, false)
            lastPrecursor = currentMillis
        }
    }
}