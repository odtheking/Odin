package me.odinclient.ui.clickgui.util

import me.odinclient.features.impl.general.ClickGUIModule
import me.odinclient.utils.render.Color

/**
 * Provides color for the click gui.
 * Based on HeroCode's gui.
 *
 * @author HeroCode, Aton
 */
object ColorUtil {

    inline val clickGUIColor: Color
        get() = ClickGUIModule.color

    val outlineColor : Int
        get() = clickGUIColor.darker().rgba

    val hoverColor: Int
        get() = clickGUIColor.darker(0.5f).rgba

    val buttonColor = Color(38, 38, 38).rgba

    fun moduleColor(boolean: Boolean): Int = if (boolean) clickGUIColor.rgba else moduleButtonColor

    const val moduleButtonColor = -0xe5e5e6
    const val boxHoverColor = 0x55111111
    const val textColor = -0x101011
    const val sliderBackgroundColor = -0xefeff0
    const val elementBackground = -0x4DD9D9DA

    fun Color.withAlpha(alpha: Float): Color {
        return Color(r, g, b, alpha)
    }

    fun Color.withAlpha(alpha: Int): Color {
        return Color(r, g, b, alpha / 255f)
    }

    fun Color.darker(factor: Float = 0.7f): Color {
        return Color(hue, saturation, brightness * factor, alpha)
    }

    fun Color.hsbMax(): Color {
        return Color(hue, 1f, 1f)
    }
}