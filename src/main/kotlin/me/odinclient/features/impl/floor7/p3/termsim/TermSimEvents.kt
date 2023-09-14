package me.odinclient.features.impl.floor7.p3.termsim

import net.minecraftforge.event.entity.player.ItemTooltipEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import me.odinclient.OdinClient.Companion.mc

object TermSimEvents {
    @SubscribeEvent
    fun onDrawTooltip(event: ItemTooltipEvent) {
        if (mc.currentScreen is TermSimGui && event.itemStack?.displayName == "") event.toolTip.clear()
    }
}