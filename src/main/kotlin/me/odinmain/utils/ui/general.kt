package me.odinmain.utils.ui

import com.github.stivais.ui.color.Color
import com.github.stivais.ui.constraints.Constraints
import com.github.stivais.ui.constraints.measurements.Percent
import com.github.stivais.ui.constraints.positions.Linked
import com.github.stivais.ui.elements.impl.TextScope
import com.github.stivais.ui.elements.scope.ElementDSL
import com.github.stivais.ui.renderer.Font
import me.odinmain.features.Module
import me.odinmain.features.Module.HUDScope
import me.odinmain.features.settings.impl.ColorSetting
import me.odinmain.features.settings.impl.SelectorSetting

// todo: better solution
infix fun TextScope.and(other: TextScope) {
    other.element.constraints.x = Linked(element)
    other.size = size
}

fun ElementDSL.outline(constraints: Constraints, color: Color, radius: FloatArray? = null) = block(constraints, Color.TRANSPARENT, radius).outline(color)

/**
 * Makes a HUD, that uses common settings found in text-based HUDs.
 *
 * @param x Default position on screen.
 * @param y Default position on screen.
 */
@Suppress("FunctionName")
inline fun Module.TextHUD(
    x: Percent,
    y: Percent,
    crossinline block: HUDScope.(Color, Font) -> Unit
): Module.HUD {

    val colorSetting = ColorSetting("Color", Color.RGB(50, 150, 220), allowAlpha = false).hide()
    // todo: make a custom ui setting that looks similar to selector setting, however each value corresponds to the font
    val fontSetting = SelectorSetting("Font", arrayListOf("Regular", "Minecraft")).hide()

    register(
        colorSetting,
        fontSetting,
    )
    return this.HUD(x, y) {
        val font = when (fontSetting.value) {
            1 -> Font("Minecraft", "/assets/odinmain/fonts/Minecraft-Regular.otf")
            else -> Font("Regular", "/assets/odinmain/fonts/Regular.otf")
        }
        block(colorSetting.value, font)
    }.apply {
        settings.add(colorSetting)
        settings.add(fontSetting)
    }
}