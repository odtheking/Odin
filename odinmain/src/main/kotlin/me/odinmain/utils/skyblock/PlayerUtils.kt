package me.odinmain.utils.skyblock

import me.odinmain.OdinMain.mc
import me.odinmain.ui.clickgui.util.ColorUtil.addColor


object PlayerUtils {
    var shouldBypassVolume = false

    /**
     * Plays a sound at a specified volume and pitch, bypassing the default volume setting.
     *
     * @param sound The identifier of the sound to be played.
     * @param volume The volume at which the sound should be played.
     * @param pitch The pitch at which the sound should be played.
     *
     * @author Aton
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
     *
     * @author Odtheking, Bonsai
     */
    fun alert(title: String, subtitle: String = "", fadeIn: Int= 10, time: Int = 40, fadeOut: Int = 10, playSound: Boolean = true) {
        if (playSound) playLoudSound("note.pling", 100f, 1f)
        showTitle(title, subtitle, fadeIn, time, fadeOut)
    }

    fun showTitle(title: String, subtitle: String, fadeIn: Int, time: Int, fadeOut: Int) {
        val gui = mc.ingameGUI
        gui.displayTitle(addColor(title), null, fadeIn, time, fadeOut)
        gui.displayTitle(null, addColor(subtitle), fadeIn, time, fadeOut)
        gui.displayTitle(null, null, fadeIn, time, fadeOut)
    }

    inline val posX get() = mc.thePlayer.posX
    inline val posY get() = mc.thePlayer.posY
    inline val posZ get() = mc.thePlayer.posZ
}