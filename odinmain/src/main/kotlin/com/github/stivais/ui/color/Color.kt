package com.github.stivais.ui.color

import com.github.stivais.ui.animation.Animation
import com.github.stivais.ui.animation.Animations
import com.github.stivais.ui.utils.getRGBA
import kotlin.math.roundToInt
import java.awt.Color as JColor

fun Color.toHSB(): Color.HSB {
    return Color.HSB(
        JColor.RGBtoHSB(
            red,
            green,
            blue,
            FloatArray(size = 3)
        ),
        a / 255f
    )
}

inline fun Color(crossinline getter: () -> Int): Color = object : Color {
    override val rgba: Int
        get() {
            return getter()
        }
}

interface Color {

    val rgba: Int

    val red
        get() = rgba shr 16 and 0xFF

    val green
        get() = rgba shr 8 and 0xFF

    val blue
        get() = rgba and 0xFF

    val a
        get() = rgba shr 24 and 0xFF

    @JvmInline
    value class RGB(override val rgba: Int) : Color {
        constructor(red: Int, green: Int, blue: Int, alpha: Float = 1f) : this(getRGBA(red, green, blue, (alpha * 255).roundToInt()))
    }

    open class HSB(hue: Float, saturation: Float, brightness: Float, alpha: Float = 1f) : Color {

        constructor(hsb: FloatArray, alpha: Float = 1f) : this(hsb[0], hsb[1], hsb[2], alpha)

        var hue = hue
            set(value) {
                field = value
                needsUpdate = true
            }

        var saturation = saturation
            set(value) {
                field = value
                needsUpdate = true
            }

        var brightness = brightness
            set(value) {
                field = value
                needsUpdate = true
            }

        var alpha = alpha
            set(value) {
                field = value
                needsUpdate = true
            }

        private var needsUpdate: Boolean = true

        override var rgba: Int = 0
            get() {
                if (needsUpdate) {
                    field = (JColor.HSBtoRGB(hue, saturation, brightness) and 0X00FFFFFF) or ((alpha * 255).toInt() shl 24)
                    needsUpdate = false
                }
                return field
            }
    }

    class Animated(from: Color, to: Color) : Color {

        constructor(from: Color, to: Color, swapIf: Boolean) : this(from, to) {
            if (swapIf) {
                swap()
                current = color1.rgba
            }
        }

        private var color1: Color = from
        private var color2: Color = to

        private var animation: Animation? = null

        var current: Int = color1.rgba
        var from: Int = color1.rgba

        override val rgba: Int
            get() {
                if (animation != null) {
                    val progress = animation!!.get()
                    val to = color2.rgba
                    current = getRGBA(
                        (from.red + (to.red - from.red) * progress).toInt(),
                        (from.green + (to.green - from.green) * progress).toInt(),
                        (from.blue + (to.blue - from.blue) * progress).toInt(),
                        (from.alpha + (to.alpha - from.alpha) * progress).toInt()
                    )
                    if (animation!!.finished) {
                        animation = null
                        swap()
                    }
                    return current
                }
                return color1.rgba
            }

        fun animate(duration: Float = 0f, type: Animations) {
            if (duration == 0f) {
                swap()
                current = color1.rgba // here so it updates if you swap a color and want to animate it later
            } else {
                if (animation != null) {
                    swap()
                    animation = Animation(duration * animation!!.get(), type)
                    from = current
                } else {
                    animation = Animation(duration, type)
                    from = color1.rgba
                }
            }
        }

        private fun swap() {
            val temp = color2
            color2 = color1
            color1 = temp
        }
    }

    companion object {
        // todo: add more
        @JvmField
        val TRANSPARENT = RGB(0, 0, 0, 0f)

        @JvmField
        val WHITE = RGB(255, 255, 255)

        @JvmField
        val BLACK = RGB(0, 0, 0)
    }
}

// util:

inline val Int.red
    get() = this shr 16 and 0xFF

inline val Int.green
    get() = this shr 8 and 0xFF

inline val Int.blue
    get() = this and 0xFF

inline val Int.alpha
    get() = this shr 24 and 0xFF

fun Int.brighter(amount: Double = 1.2): Int {
    return getRGBA(
        (red * amount).roundToInt().coerceIn(0, 255),
        (green * amount).roundToInt().coerceIn(0, 255),
        (blue * amount).roundToInt().coerceIn(0, 255),
        (alpha * amount).roundToInt().coerceIn(0, 255)
    )
}