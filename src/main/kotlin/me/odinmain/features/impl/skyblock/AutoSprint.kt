package me.odinmain.features.impl.skyblock

import me.odinmain.features.Module
import net.minecraft.client.settings.KeyBinding.setKeyBindState
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent

object AutoSprint : Module(
    name = "Auto Sprint",
    desc = "Automatically makes you sprint."
) {
    @SubscribeEvent
    fun onTick(event: ClientTickEvent) {
        setKeyBindState(mc.gameSettings.keyBindSprint.keyCode, true)
    }
}