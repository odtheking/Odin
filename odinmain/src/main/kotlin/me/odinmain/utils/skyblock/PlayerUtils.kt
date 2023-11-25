package me.odinmain.utils.skyblock

import me.odinmain.OdinMain.mc
import me.odinmain.utils.floored
import net.minecraft.util.Vec3i


object PlayerUtils {
    var shouldBypassVolume = false

    /**
     * Plays a sound at a specified volume and pitch, bypassing the default volume setting.
     *
     * @param sound The identifier of the sound to be played.
     * @param volume The volume at which the sound should be played.
     * @param pitch The pitch at which the sound should be played.
     */
    fun playLoudSound(sound: String?, volume: Float, pitch: Float) {
        shouldBypassVolume = true
        mc.thePlayer?.playSound(sound, volume, pitch)
        shouldBypassVolume = false
    }

    /**
     * Displays an alert on screen and plays a sound
     *
     * @param title String to be displayed.
     * @param playSound Toggle for sound.
     */
    fun alert(title: String, playSound: Boolean = true) {
        if (playSound) mc.thePlayer.playSound("note.pling", 100f, 1f)
        mc.ingameGUI.run {
            displayTitle(title, null, 10, 100, 10)
            displayTitle(null, "", 10, 100, 10)
            displayTitle(null, null, 10, 100, 10)
        }
    }
    fun getFlooredPlayerCoords(): Vec3i = mc.thePlayer.positionVector.floored()

    inline val posX get() = mc.thePlayer.posX
    inline val posY get() = mc.thePlayer.posY
    inline val posZ get() = mc.thePlayer.posZ
}