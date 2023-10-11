package me.odinclient.features.impl.dungeon

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.odinclient.utils.skyblock.PlayerUtils
import me.odinmain.OdinMain.scope
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.utils.skyblock.ItemUtils.unformattedName

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