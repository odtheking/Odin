package me.odinmain.utils.skyblock

import me.odinmain.OdinMain.mc
import me.odinmain.utils.VecUtils.floored
import net.minecraft.client.settings.KeyBinding
import net.minecraft.util.Vec3i


object PlayerUtils {

    /**
     * Right-clicks the next tick
     */
    fun rightClick() {
        KeyBinding.onTick(-99) // Simple way of making completely sure the right-clicks are sent at the same time as vanilla ones.
    }


    fun alert(title: String, playSound: Boolean = true) {
        if (playSound) mc.thePlayer.playSound("note.pling", 100f, 1f)
        mc.ingameGUI.run {
            displayTitle(title, null, 10, 250, 10)
            displayTitle(null, "", 10, 250, 10)
            displayTitle(null, null, 10, 250, 10)
        }
    }

    fun getFlooredPlayerCoords(): Vec3i = mc.thePlayer.positionVector.floored()

    inline val posX get() = mc.thePlayer.posX
    inline val posY get() = mc.thePlayer.posY
    inline val posZ get() = mc.thePlayer.posZ
}