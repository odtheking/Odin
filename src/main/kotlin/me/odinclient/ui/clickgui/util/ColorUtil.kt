package me.odinclient.ui.clickgui.util

import cc.polyfrost.oneconfig.config.core.OneColor
import me.odinclient.features.general.ClickGui
import java.awt.Color

/**
 * Provides color for the click gui.
 * Based on HeroCode's gui.
 *
 * @author HeroCode, Aton
 */
object ColorUtil {
    val clickGUIColor: Color
        get() = ClickGui.color

    val clickGuiSecondColor: Color
        get() = ClickGui.secondColor

    val outlineColor : Int
        get() = clickGUIColor.darker().rgb

    val hoverColor: Int
        get() {
            val temp = clickGUIColor.darker()
            val scale = 0.5
            return OneColor(((temp.red*scale).toInt()), (temp.green*scale).toInt(), (temp.blue*scale).toInt()).rgb
        }

    val buttonColor = OneColor(38, 38, 38).rgb


    fun moduleColor(boolean: Boolean): Int = if (boolean) outlineColor else moduleButtonColor
    fun buttonColor(boolean: Boolean): Int = if (boolean) clickGUIColor.rgb else buttonColor

    fun sliderColor(dragging: Boolean): Int = clickGUIColor.withAlpha(if (dragging) 250 else 200).rgb

    const val moduleButtonColor = -0xe5e5e6
    const val boxHoverColor = 0x55111111
    const val textColor = -0x101011
    const val sliderBackgroundColor = -0xefeff0
    const val clickableColor = -0xDCDCDD
    const val elementBackground = -0x4DD9D9DA

    fun Color.withAlpha(alpha: Int) = Color(this.red, this.green, this.blue, alpha)
    fun Int.withAlpha(alpha: Int) = Color(this).withAlpha(alpha).rgb
}