package me.odinclient.features.qol

import me.odinclient.OdinClient.Companion.config
import me.odinclient.OdinClient.Companion.mc
import net.minecraft.client.settings.KeyBinding.setKeyBindState
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent

object AutoSprint {
    @SubscribeEvent
    fun onTick(event: ClientTickEvent) {
        if (!config.autoSprint) return
        setKeyBindState(mc.gameSettings.keyBindSprint.keyCode, true)
    }
}