package me.odinmain.ui.clickgui.util

import me.odinmain.features.impl.render.ClickGUIModule
import me.odinmain.utils.render.Color

object ColorUtil {

    inline val clickGUIColor: Color
        get() = ClickGUIModule.color

    val buttonColor = Color(38, 38, 38)

    val moduleButtonColor = Color(26, 26, 26)
    val elementBackground = Color(37, 38, 38, 0.7f)
    val textColor = Color(239, 239, 239)

    /**
     * Changes or creates a new color with the given alpha. (There is no checks if alpha is in valid range for now.)
     */
    fun Color.withAlpha(alpha: Float, newInstance: Boolean = true): Color {
        if (!newInstance) {
            this.alpha = alpha
            return this
        }
        return Color(r, g, b, alpha)
    }

    fun Color.brighter(factor: Float = 1.3f): Color {
        return Color(hue, saturation, (brightness * factor.coerceAtLeast(1f)).coerceAtMost(1f), alpha)
    }

    fun Color.darker(factor: Float = 0.7f): Color {
        return Color(hue, saturation, brightness * factor, alpha)
    }

    fun Color.darkerIf(condition: Boolean, factor: Float = 0.7f): Color {
        return if (condition) darker(factor) else this
    }

    fun Color.hsbMax(): Color {
        return Color(hue, 1f, 1f)
    }

    /**
     * Replaces the easier to type '&' color codes with proper color codes in a string.
     *
     * @param message The string to add color codes to
     * @return the formatted message
     */
    fun addColor(message: String?): String {
        return message.toString().replace("(?<!\\\\)&(?![^0-9a-fk-or]|$)".toRegex(), "\u00a7")
    }
}