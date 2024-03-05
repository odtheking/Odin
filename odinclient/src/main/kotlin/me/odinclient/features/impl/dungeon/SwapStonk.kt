package me.odinclient.features.impl.dungeon

import me.odinclient.utils.skyblock.PlayerUtils.leftClick
import me.odinclient.utils.skyblock.PlayerUtils.swapToIndex
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.KeybindSetting
import me.odinmain.features.settings.impl.Keybinding
import me.odinmain.features.settings.impl.NumberSetting
import me.odinmain.features.settings.impl.SelectorSetting
import me.odinmain.utils.runIn
import me.odinmain.utils.skyblock.getItemSlot
import me.odinmain.utils.skyblock.modMessage
import org.lwjgl.input.Keyboard

object SwapStonk : Module(
    name = "Swap Stonk",
    description = "Does a swap stonk when you press the keybind.",
    category = Category.DUNGEON,
    key = null
) {
    private val keybind: Keybinding by KeybindSetting("Keybind", Keyboard.KEY_NONE, "Press to perform a swap stonk")
        .onPress {
            if (enabled) {
                val slot = getItemSlot(if (pickaxe == 1) "Stonk" else "Pickaxe", true)
                if (slot in 0..8) {
                    val originalItem = mc.thePlayer?.inventory?.currentItem ?: 0
                    if (originalItem == slot) return@onPress
                    leftClick()
                    swapToIndex(slot!!)
                    runIn(speed) { swapToIndex(originalItem) }
                } else {
                    modMessage("Couldn't find pickaxe.")
                }
            }
        }

    private val pickaxe: Int by SelectorSetting("Type", "Pickaxe", arrayListOf("Pickaxe", "Stonk"), description = "The type of pickaxe to use")

    private val speed: Int by NumberSetting("Swap back speed", 2, 1, 5, description = "Delay between swapping back", unit = " ticks")
}