package me.odinmain.ui.clickgui.util

import me.odinmain.features.impl.render.ClickGUI
import me.odinmain.utils.render.Color

object ColorUtil {

    inline val clickGUIColor: Color
        get() = ClickGUI.oldColor

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

    fun Color.multiplyAlpha(factor: Float): Color {
        return Color(r, g, b, (alpha * factor).coerceIn(0f, 1f))
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
}