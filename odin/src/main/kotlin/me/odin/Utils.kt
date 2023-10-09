package me.odin


import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent

object Utils {
    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        modMessage("ODIN")
    }




}