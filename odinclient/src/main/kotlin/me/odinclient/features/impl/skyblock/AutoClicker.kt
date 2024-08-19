package me.odinclient.features.impl.skyblock

import me.odinclient.utils.skyblock.PlayerUtils.leftClick
import me.odinclient.utils.skyblock.PlayerUtils.rightClick
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.DropdownSetting
import me.odinmain.features.settings.impl.KeybindSetting
import me.odinmain.features.settings.impl.Keybinding
import me.odinmain.features.settings.impl.NumberSetting
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.input.Keyboard

object AutoClicker : Module(
    name = "Auto Clicker",
    description = "Automatically clicks for you.",
    category = Category.SKYBLOCK
) {
    private val leftclickcps: Boolean by DropdownSetting("Left Click Dropdown", false)
    private val leftcps: Double by NumberSetting("Clicks Per Second", 5.0, 3.0, 15.0, .5, false, "The amount of clicks per second to perform.").withDependency { AutoClicker.leftclickcps }
    private val leftkeybind: Keybinding by KeybindSetting("Auto Clicker Left Keybind", Keyboard.KEY_NONE, "Starts and stops the Auto Clicker for left clicks.").withDependency { AutoClicker.leftclickcps }
    private val rightclickcps: Boolean by DropdownSetting("Right Click Dropdown", false)
    private val rightcps: Double by NumberSetting("Clicks Per Second", 5.0, 3.0, 15.0, .5, false, "The amount of clicks per second to perform.").withDependency { AutoClicker.rightclickcps }
    private val rightkeybind: Keybinding by KeybindSetting("Auto Clicker Right Keybind", Keyboard.KEY_NONE, "Starts and stops the Auto Clicker for right clicks.").withDependency { AutoClicker.rightclickcps }

    private var nextLeftClick: Long = 0
    private var nextRightClick: Long = 0

    // Track previous key states and toggle state
    private var wasLeftKeyDown = false
    private var wasRightKeyDown = false
    private var isLeftClicking = false
    private var isRightClicking = false

    @SubscribeEvent
    fun onRenderWorldLast(event: RenderWorldLastEvent) {
        val nowMillis = System.currentTimeMillis()

        // Handle left click toggle
        if (leftclickcps) {
            if (leftkeybind.isDown() && !wasLeftKeyDown) {
                // Toggle auto clicking
                isLeftClicking = !isLeftClicking
                nextLeftClick = if (isLeftClicking) nowMillis else Long.MAX_VALUE
            }

            if (isLeftClicking && nowMillis >= nextLeftClick) {
                nextLeftClick = nowMillis + (1000 / leftcps.toLong() + ((Math.random() - .5) * 60.0).toLong())
                leftClick()
            }
        }

        // Handle right click toggle
        if (rightclickcps) {
            if (rightkeybind.isDown() && !wasRightKeyDown) {
                // Toggle auto clicking
                isRightClicking = !isRightClicking
                nextRightClick = if (isRightClicking) nowMillis else Long.MAX_VALUE
            }

            if (isRightClicking && nowMillis >= nextRightClick) {
                nextRightClick = nowMillis + (1000 / rightcps.toLong() + ((Math.random() - .5) * 60.0).toLong())
                rightClick()
            }
        }

        // Update previous key states
        wasLeftKeyDown = leftkeybind.isDown()
        wasRightKeyDown = rightkeybind.isDown()
    }
}
