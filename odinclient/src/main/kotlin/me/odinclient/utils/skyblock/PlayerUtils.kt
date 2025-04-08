package me.odinclient.utils.skyblock

import me.odinmain.OdinMain.mc
import me.odinmain.utils.runIn
import net.minecraft.client.settings.KeyBinding


object PlayerUtils {

    /**
     * Right-clicks the next tick
     */
    fun rightClick() {
        KeyBinding.onTick(mc.gameSettings.keyBindUseItem.keyCode) // Simple way of making completely sure the right-clicks are sent at the same time as vanilla ones.
    }

    /**
     * Left-clicks the next tick
     */
    fun leftClick() {
        KeyBinding.onTick(mc.gameSettings.keyBindAttack.keyCode) // Simple way of making completely sure the left-clicks are sent at the same time as vanilla ones.
    }

    fun dropItem(dropAll: Boolean = false, delay: Int = 1) {
        runIn(delay.coerceAtLeast(1)) { mc.thePlayer.dropOneItem(dropAll) } // just so that this runs on tick properly
    }

    fun swapToIndex(index: Int) {
        KeyBinding.onTick(mc.gameSettings.keyBindsHotbar[index].keyCode)
    }
}