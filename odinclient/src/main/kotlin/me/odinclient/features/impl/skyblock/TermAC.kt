package me.odinclient.features.impl.skyblock

import me.odinclient.utils.skyblock.PlayerUtils.leftClick
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.NumberSetting
import me.odinmain.utils.skyblock.isHolding
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object TermAC : Module(
    name = "Terminator Ac",
    description = "Randomized auto-clicker for Terminator's salvation ability, enabled when holding right click.",
    category = Category.SKYBLOCK
) {
    private val cps: Double by NumberSetting("Clicks Per Second", 5.0, 3.0, 15.0, .5, false, "The amount of clicks per second to perform.")
    private var nextClick = .0

    @SubscribeEvent
    fun onRenderWorldLast(event: RenderWorldLastEvent) {
        if (!isHolding("TERMINATOR") || !mc.gameSettings.keyBindUseItem.isKeyDown) return
        val nowMillis = System.currentTimeMillis()
        if (nowMillis < nextClick) return
        nextClick = nowMillis + ((1000 / cps) + ((Math.random() - .5) * 60.0))
        leftClick()
    }
}