package me.odinmain.utils.ui

import me.odinmain.utils.render.Color

/**
 * Object which contains a list of [Colors][Color] representing color codes in Minecraft.
 */
object Colors {

    @JvmField
    val MINECRAFT_DARK_BLUE: Color = Color(0, 0, 170)

    @JvmField
    val MINECRAFT_DARK_GREEN: Color = Color(0, 170, 0)

    @JvmField
    val MINECRAFT_DARK_AQUA: Color = Color(0, 170, 170)

    @JvmField
    val MINECRAFT_DARK_RED: Color = Color(170, 0, 0)

    @JvmField
    val MINECRAFT_DARK_PURPLE: Color = Color(170, 0, 170)

    @JvmField
    val MINECRAFT_GOLD: Color = Color(255, 170, 0)

    @JvmField
    val MINECRAFT_GRAY: Color = Color(170, 170, 170)

    @JvmField
    val MINECRAFT_DARK_GRAY: Color = Color(85, 85, 85)

    @JvmField
    val MINECRAFT_BLUE: Color = Color(85, 85, 255)

    @JvmField
    val MINECRAFT_GREEN: Color = Color(85, 255, 85)

    @JvmField
    val MINECRAFT_AQUA: Color = Color(85, 255, 255)

    @JvmField
    val MINECRAFT_RED: Color = Color(255, 85, 85)

    @JvmField
    val MINECRAFT_LIGHT_PURPLE: Color = Color(255, 85, 255)

    @JvmField
    val MINECRAFT_YELLOW: Color = Color(255, 255, 85)

    @JvmField
    val WHITE: Color = Color(255, 255, 255)

    @JvmField
    val BLACK: Color = Color(0, 0, 0)

    val TRANSPARENT: Color = Color(0, 0, 0, 0f)
}
