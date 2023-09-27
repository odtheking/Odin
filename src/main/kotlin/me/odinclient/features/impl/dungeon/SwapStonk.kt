package me.odinclient.features.impl.dungeon

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.odinclient.OdinClient.Companion.scope
import me.odinclient.features.Category
import me.odinclient.features.Module
import me.odinclient.utils.skyblock.ItemUtils.unformattedName
import me.odinclient.utils.skyblock.PlayerUtils

object SwapStonk : Module(
    name = "Swap Stonk",
    description = "Does a swap stonk when you press the keybind.",
    category = Category.DUNGEON,
    tag = TagType.NEW
) {
    override fun onKeybind() {
        scope.launch {
            val originalItem = mc.thePlayer.heldItem.unformattedName
            PlayerUtils.leftClick()
            PlayerUtils.swapToItem("Pickaxe", true)
            delay(100)
            PlayerUtils.swapToItem(originalItem, true)
        }
    }
}