package me.odinclient.features.general

import me.odinclient.OdinClient.Companion.config
import me.odinclient.OdinClient.Companion.mc
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent

object Camera {
    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (!config.frontCamera && mc.gameSettings.thirdPersonView == 2)
            mc.gameSettings.thirdPersonView = 0
    }

    /*
        Camera distance is in MixinEntityRenderer
     */
}