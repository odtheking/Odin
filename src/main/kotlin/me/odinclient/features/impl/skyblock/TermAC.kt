package me.odinclient.features.impl.skyblock

import me.odinclient.OdinClient.Companion.mc
import me.odinclient.features.Category
import me.odinclient.features.Module
import me.odinclient.features.settings.impl.NumberSetting
import me.odinclient.utils.ServerUtils
import me.odinclient.utils.skyblock.ItemUtils.itemID
import me.odinclient.utils.skyblock.PlayerUtils.rightClick
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object TermAC : Module(
    "Terminator AC",
    description = "Randomized auto-clicker for Terminator, delay adapts to the server's current TPS, meaning you should never get kicked.",
    category = Category.SKYBLOCK
) {
    private val cps: Int by NumberSetting("Clicks per second", 20, 5, 30, 1, false, "The amount of clicks per second to perform. This will still be multiplied by the server's TPS")
    private var nextClick = .0

    @SubscribeEvent
    fun onRenderWorldLast(event: RenderWorldLastEvent) {
        if (mc.thePlayer?.heldItem?.itemID != "TERMINATOR" || !mc.gameSettings.keyBindUseItem.isKeyDown) return
        val nowMillis = System.currentTimeMillis()
        if (nowMillis < nextClick) return
        nextClick =
            nowMillis + (
                (1000 / cps) * // cps
                (20 / ServerUtils.averageTps) + // multiply by the server's TPS to not get kicked
                ((Math.random() - .5) * 30.0) // randomize the click time by +- 15ms
            )
        rightClick()
    }
}
