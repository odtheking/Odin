package me.odinmain.utils.skyblock

import me.odinmain.OdinMain.mc
import me.odinmain.utils.floored
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.Renderer
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.item.ItemStack


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
    fun alert(title: String, time: Int = 20, color: Color = Color.WHITE, playSound: Boolean = true, displayText: Boolean = true) {
        if (playSound) playLoudSound("note.pling", 100f, 1f)
        if (displayText) Renderer.displayTitle(title , time, color = color)
    }

    fun dropItem() {
        mc.thePlayer.dropOneItem(false)
    }

    inline val posX get() = mc.thePlayer.posX
    inline val posY get() = mc.thePlayer.posY
    inline val posZ get() = mc.thePlayer.posZ

    val posFloored
        get() = mc.thePlayer.positionVector.floored()

    fun EntityPlayerSP?.isHolding(vararg names: String, ignoreCase: Boolean = false, mode: Int = 0): Boolean {
        val regex = Regex("${if (ignoreCase) "(?i)" else ""}${names.joinToString("|")}")
        return this.isHolding(regex, mode)
    }

    fun EntityPlayerSP?.isHolding(regex: Regex, mode: Int = 0): Boolean {
        return this.isHolding { it?.run {
            when (mode) {
                0 -> displayName.contains(regex) || itemID.matches(regex)
                1 -> displayName.contains(regex)
                2 -> itemID.matches(regex)
                else -> false
            } } == true
        }
    }

    private fun EntityPlayerSP?.isHolding(predicate: (ItemStack?) -> Boolean): Boolean {
        if (this == null) return false
        return predicate(this.heldItem)
    }
}