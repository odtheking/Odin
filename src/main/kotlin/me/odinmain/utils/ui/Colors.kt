package me.odinmain.utils.ui

import com.github.stivais.aurora.color.Color
import com.github.stivais.aurora.color.Color.RGB

/**
 * Object which contains a list of [Colors][Color] representing color codes in Minecraft.
 */
object Colors {
    @JvmField
    val MINECRAFT_DARK_BLUE: Color = RGB(0, 0, 170)

    @JvmField
    val MINECRAFT_DARK_GREEN: Color = RGB(0, 170, 0)

    @JvmField
    val MINECRAFT_DARK_AQUA: Color = RGB(0, 170, 170)

    @JvmField
    val MINECRAFT_DARK_RED: Color = RGB(170, 0, 0)

    @JvmField
    val MINECRAFT_DARK_PURPLE: Color = RGB(170, 0, 170)

    @JvmField
    val MINECRAFT_GOLD: Color = RGB(255, 170, 0)

    @JvmField
    val MINECRAFT_GRAY: Color = RGB(170, 170, 170)

    @JvmField
    val MINECRAFT_DARK_GRAY: Color = RGB(85, 85, 85)

    @JvmField
    val MINECRAFT_BLUE: Color = RGB(85, 85, 255)

    @JvmField
    val MINECRAFT_GREEN: Color = RGB(85, 255, 85)

    @JvmField
    val MINECRAFT_AQUA: Color = RGB(85, 255, 255)

    @JvmField
    val MINECRAFT_RED: Color = RGB(255, 85, 85)

    @JvmField
    val MINECRAFT_LIGHT_PURPLE: Color = RGB(255, 85, 255)

    @JvmField
    val MINECRAFT_YELLOW: Color = RGB(255, 255, 85)

    @JvmField
    val WHITE: Color = RGB(255, 255, 255)
}