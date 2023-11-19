package me.odinclient.features.impl.dungeon

import me.odinclient.utils.skyblock.PlayerUtils
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.utils.runIn

object SwapStonk : Module(
    name = "Swap Stonk",
    description = "Does a swap stonk when you press the keybind.",
    category = Category.DUNGEON,
    tag = TagType.NEW
) {
    override fun onKeybind() {
        val originalItem = mc.thePlayer?.inventory?.currentItem ?: 0
        PlayerUtils.leftClick()
        PlayerUtils.swapToItem("Pickaxe", true)
        runIn(2) {
            PlayerUtils.swapToIndex(originalItem)
        }
    }
}