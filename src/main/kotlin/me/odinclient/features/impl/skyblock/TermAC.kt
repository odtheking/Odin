package me.odinclient.features.impl.skyblock

import me.odinclient.OdinClient.Companion.mc
import me.odinclient.features.Category
import me.odinclient.features.Module
import me.odinclient.utils.ServerUtils
import me.odinclient.utils.skyblock.ItemUtils.itemID
import me.odinclient.utils.skyblock.PlayerUtils.rightClick
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object TermAC : Module(
    "Terminator AC",
    description = "Randomized auto-clicker for Terminator, clicks at the servers current TPS, meaning you should never get kicked.",
    category = Category.SKYBLOCK
) {
    private var nextClick = .0

    @SubscribeEvent
    fun onRenderWorldLast(event: RenderWorldLastEvent) {
        if (mc.thePlayer?.heldItem?.itemID != "TERMINATOR" || !mc.gameSettings.keyBindUseItem.isKeyDown) return
        val nowMillis = System.currentTimeMillis()
        if (nowMillis < nextClick) return
        nextClick = nowMillis + (1000 / ServerUtils.averageTps) - 30 + (Math.random() * 60)
        rightClick()
    }
}
