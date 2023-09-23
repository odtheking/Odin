package me.odinclient.features.impl.skyblock

import me.odinclient.features.Category
import me.odinclient.features.Module
import me.odinclient.features.settings.impl.NumberSetting
import me.odinclient.utils.skyblock.ItemUtils.itemID
import me.odinclient.utils.skyblock.PlayerUtils.leftClick
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object TermAC : Module(
    "Terminator AC",
    description = "Randomized auto-clicker for Terminator's salvation ability, enabled when holding right click.",
    category = Category.SKYBLOCK
) {
    private val cps: Double by NumberSetting("Clicks per second", 5.0, 3.0, 12.0, .5, false, "The amount of clicks per second to perform.")
    private var nextClick = .0

    @SubscribeEvent
    fun onRenderWorldLast(event: RenderWorldLastEvent) {
        if (mc.thePlayer?.heldItem?.itemID != "TERMINATOR" || !mc.gameSettings.keyBindUseItem.isKeyDown) return
        val nowMillis = System.currentTimeMillis()
        if (nowMillis < nextClick) return
        nextClick = nowMillis + ((1000 / cps) + ((Math.random() - .5) * 60.0))
        leftClick()
    }
}