package me.odinclient.features.qol

import me.odinclient.OdinClient.Companion.config
import me.odinclient.OdinClient.Companion.mc
import me.odinclient.utils.skyblock.PlayerUtils.rightClick
import me.odinclient.utils.Server
import me.odinclient.utils.skyblock.ItemUtils.itemID
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object TermAC {
    private var nextClick = Double.MIN_VALUE

    @SubscribeEvent
    fun onRenderWorldLast(event: RenderWorldLastEvent) {
        if (!config.terminatorAC || mc.thePlayer?.heldItem?.itemID != "TERMINATOR" || !mc.gameSettings.keyBindUseItem.isKeyDown) return
        val nowMillis = System.currentTimeMillis()
        if (nowMillis < nextClick) return
        nextClick = nowMillis + (1000 / Server.averageTps) - 15 + (Math.random() * 30)
        rightClick()
    }
}
