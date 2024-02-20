package me.odinmain.utils.skyblock

import me.odinmain.OdinMain.mc
import me.odinmain.ui.clickgui.util.ColorUtil.addColor
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.world.RenderUtils


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
    fun alert(title: String, time: Int = 60, color: Color = Color.WHITE, playSound: Boolean = true, displayText: Boolean = true) {
        if (playSound) playLoudSound("note.pling", 100f, 1f)
        RenderUtils.displayTitle(if(displayText) title else "", time, color = color)
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