package me.odinclient.features.impl.skyblock

import me.odinclient.features.Category
import me.odinclient.features.Module
import net.minecraft.client.settings.KeyBinding.setKeyBindState
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent

object AutoSprint : Module(
    "Auto Sprint",
    description = "Automatically makes you sprint.",
    category = Category.SKYBLOCK
) {
    @SubscribeEvent
    fun onTick(event: ClientTickEvent) {
        setKeyBindState(mc.gameSettings.keyBindSprint.keyCode, true)
    }
}