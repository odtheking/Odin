package me.odinclient.features.impl.skyblock

import me.odinclient.utils.skyblock.PlayerUtils.leftClick
import me.odinclient.utils.skyblock.PlayerUtils.rightClick
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.NumberSetting
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object AutoClicker : Module(
    name = "Auto Clicker",
    description = "Auto clicker with options for left-click, right-click, or both.",
    category = Category.SKYBLOCK
) {
    private val enableLeftClick by BooleanSetting("Enable Left Click", true, description = "Enable auto-clicking for left-click.")
    private val enableRightClick by BooleanSetting("Enable Right Click", true, description = "Enable auto-clicking for right-click.")
    private val leftCps by NumberSetting("Left Clicks Per Second", 5.0, 3.0, 15.0, .5, false, description = "The amount of left clicks per second to perform.")
    private val rightCps by NumberSetting("Right Clicks Per Second", 5.0, 3.0, 15.0, .5, false, description = "The amount of right clicks per second to perform.")

    private var nextLeftClick = .0
    private var nextRightClick = .0

    @SubscribeEvent
    fun onRenderWorldLast(event: RenderWorldLastEvent) {
        val nowMillis = System.currentTimeMillis()

        if (enableLeftClick && mc.gameSettings.keyBindAttack.isKeyDown && nowMillis >= nextLeftClick) {
            nextLeftClick = nowMillis + ((1000 / leftCps) + ((Math.random() - .5) * 60.0))
            leftClick()
        }

        if (enableRightClick && mc.gameSettings.keyBindUseItem.isKeyDown && nowMillis >= nextRightClick) {
            nextRightClick = nowMillis + ((1000 / rightCps) + ((Math.random() - .5) * 60.0))
            rightClick()
        }
    }
}