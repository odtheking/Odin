@file:JvmName("ColorUtils")

package com.github.stivais.ui.color

import kotlin.math.roundToInt

inline val Int.red
    get() = this shr 16 and 0xFF

inline val Int.green
    get() = this shr 8 and 0xFF

inline val Int.blue
    get() = this and 0xFF

inline val Int.alpha
    get() = (this shr 24) and 0xFF

inline val Color.red
    get() = rgba.red

inline val Color.green
    get() = rgba.green

inline val Color.blue
    get() = rgba.blue

inline val Color.alpha
    get() = rgba.alpha

/**
 * Checks if the [Colors][Color] alpha value is 0
 */
inline val Color.isTransparent: Boolean
    get() = alpha == 0

fun getRGBA(red: Int, green: Int, blue: Int, alpha: Int): Int {
    return ((alpha shl 24) and 0xFF000000.toInt()) or ((red shl 16) and 0x00FF0000) or ((green shl 8) and 0x0000FF00) or (blue and 0x000000FF)
}

fun getRGBA(red: Int, green: Int, blue: Int, alpha: Float): Int {
    return (((alpha * 255).roundToInt() shl 24) and 0xFF000000.toInt()) or ((red shl 16) and 0x00FF0000) or ((green shl 8) and 0x0000FF00) or (blue and 0x000000FF)
}

/**
 * Copies an integer representing a color with a new alpha value provided
 */
fun Int.withAlpha(alpha: Int): Int = getRGBA(red, green, blue, alpha)

/**
 * Copies an integer representing a color with a new alpha value provided
 */
fun Int.withAlpha(alpha: Float): Int = withAlpha((alpha * 255).roundToInt())

/**
 * Copies a color with the new alpha value provided
 */
fun Color.withAlpha(alpha: Float): Color = Color.RGB(red, green, blue, alpha)

/**
 * Copies a color with the new alpha value provided
 */
fun Color.withAlpha(alpha: Int): Color = Color.RGB(red, green, blue, alpha / 255f)

/**
 * Copies a color, multiplying its alpha value by a certain factor
 */
fun Color.multiplyAlpha(factor: Float): Color = withAlpha((alpha * factor).roundToInt())

fun hexToRGBA(hex: String): Int {
    return when (hex.length) {
        7 -> {
            getRGBA(
                hex.substring(1, 3).toInt(16),
                hex.substring(3, 5).toInt(16),
                hex.substring(5, 7).toInt(16),
                255
            )
        }
        9 -> {
            getRGBA(
                hex.substring(1, 3).toInt(16),
                hex.substring(3, 5).toInt(16),
                hex.substring(5, 7).toInt(16),
                hex.substring(7, 9).toInt(16)
            )
        }
        else -> throw IllegalArgumentException("Invalid hex color format: $hex. Use #RRGGBB or #RRGGBBAA.")
    }
}

fun Color.toHexString(): String {
    return "#" + Integer.toHexString(rgba).substring(2)
}

fun Color.toHSB(): Color.HSB {
    return Color.HSB(
        java.awt.Color.RGBtoHSB(
            red,
            green,
            blue,
            FloatArray(size = 3)
        ),
        alpha / 255f
    )
}

fun Int.brighter(factor: Double = 1.0): Int {
    return darker(factor) // temp
}

fun Int.darker(factor: Double = 1.0): Int {
    return getRGBA(
        (red * factor).roundToInt().coerceIn(0, 255),
        (green * factor).roundToInt().coerceIn(0, 255),
        (blue * factor).roundToInt().coerceIn(0, 255),
        (alpha * factor).roundToInt().coerceIn(0, 255)
    )
}

inline fun color(crossinline getter: () -> Int): Color = object : Color {
    override val rgba: Int
        get() {
            return getter()
        }
}