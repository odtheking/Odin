package me.odinclient.utils.skyblock

import me.odinmain.OdinMain.mc
import me.odinmain.utils.runIn
import net.minecraft.client.settings.KeyBinding
import net.minecraft.util.Vec3


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

    fun playerSneak() {
        KeyBinding.onTick(mc.gameSettings.keyBindSneak.keyCode)
    }

    fun dropItem(dropAll: Boolean = false) {
        runIn(1) { mc.thePlayer.dropOneItem(dropAll) } // just so that this runs on tick properly
    }

    fun swapToIndex(index: Int) {
        KeyBinding.onTick(mc.gameSettings.keyBindsHotbar[index].keyCode)
    }

    fun clipTo(pos: Vec3) {
        mc.thePlayer.setPosition(pos.xCoord + 0.5, pos.yCoord, pos.zCoord + 0.5)
    }

    fun clipTo(x: Double, y: Double, z: Double) {
        mc.thePlayer.setPosition(x + 0.5, y, z + 0.5)
    }
}