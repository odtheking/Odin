package com.github.stivais.ui.color

import com.github.stivais.ui.animation.Animation
import com.github.stivais.ui.animation.Animations
import com.github.stivais.ui.elements.Element
import kotlin.math.roundToInt
import java.awt.Color as JColor

interface Color {

    val rgba: Int

    /**
     * Uses internally by [UIs][com.github.stivais.ui.UI]
     */
    fun get(element: Element): Int = rgba

    @JvmInline
    value class RGB(override val rgba: Int) : Color {
        constructor(
            red: Int,
            green: Int,
            blue: Int,
            alpha: Float = 1f
        ) : this(getRGBA(red, green, blue, (alpha * 255).roundToInt()))
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
                    field =
                        (JColor.HSBtoRGB(hue, saturation, brightness) and 0X00FFFFFF) or ((alpha * 255).toInt() shl 24)
                    needsUpdate = false
                }
                return field
            }
    }

    class Animated(from: Color, to: Color) : Color {

        constructor(from: Color, to: Color, swapIf: Boolean) : this(from, to) {
            if (swapIf) {
                swap()
            }
        }

        var animation: Animation? = null

        var color1: Color = from
        var color2: Color = to

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

        override fun get(element: Element): Int {
            // add stuff when fbo
            return rgba
        }

        fun animate(duration: Float = 0f, type: Animations): Animation? {
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
                return animation!!
            }
            return null
        }

        fun swap() {
            val temp = color2
            color2 = color1
            color1 = temp
        }
    }

    companion object {
        @JvmField
        val TRANSPARENT = RGB(0, 0, 0, 0f)

        @JvmField
        val WHITE = RGB(255, 255, 255)

        @JvmField
        val BLACK = RGB(0, 0, 0)

        @JvmField
        val RED = RGB(255, 0, 0)

        @JvmField
        val BLUE = RGB(0, 0, 255)

        @JvmField
        val GREEN = RGB(0, 255, 0)

        // Minecraft colors

        @JvmField
        val MINECRAFT_DARK_BLUE = RGB(0, 0, 170)

        @JvmField
        val MINECRAFT_DARK_GREEN = RGB(0, 170, 0)

        @JvmField
        val MINECRAFT_DARK_AQUA = RGB(0, 170, 170)

        @JvmField
        val MINECRAFT_DARK_RED = RGB(170, 0, 0)

        @JvmField
        val MINECRAFT_DARK_PURPLE = RGB(170, 0, 170)

        @JvmField
        val MINECRAFT_GOLD = RGB(255, 170, 0)

        @JvmField
        val MINECRAFT_GRAY = RGB(170, 170, 170)

        @JvmField
        val MINECRAFT_DARK_GRAY = RGB(85, 85, 85)

        @JvmField
        val MINECRAFT_BLUE = RGB(85, 85, 255)

        @JvmField
        val MINECRAFT_GREEN = RGB(85, 255, 85)

        @JvmField
        val MINECRAFT_AQUA = RGB(85, 255, 255)

        @JvmField
        val MINECRAFT_RED = RGB(255, 85, 85)

        @JvmField
        val MINECRAFT_LIGHT_PURPLE = RGB(255, 85, 255)

        @JvmField
        val MINECRAFT_YELLOW = RGB(255, 255, 85)
    }
}
