package me.odinmain.utils.render

import java.awt.Color.HSBtoRGB
import java.awt.Color.RGBtoHSB
import java.awt.Color as JavaColor

/**
 * HSB based color class.
 * Based on [PolyUI](https://github.com/Polyfrost/polyui-jvm/blob/master/src/main/kotlin/cc/polyfrost/polyui/color/Color.kt)
 */
class Color(hue: Float, saturation: Float, brightness: Float, alpha: Float = 1f) {
    constructor(hsb: FloatArray, alpha: Float = 1f) : this(hsb[0], hsb[1], hsb[2], alpha)
    constructor(r: Int, g: Int, b: Int, alpha: Float = 1f) : this(RGBtoHSB(r, g, b, FloatArray(size = 3)), alpha)
    constructor(rgba: Int) : this(rgba.red, rgba.green, rgba.blue, alpha = rgba.alpha / 255f)
    constructor(rgba: Int, alpha: Float) : this(rgba.red, rgba.green, rgba.blue, alpha)

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

    // Only used in Window, because that rendering needs java.awt.Color
    val javaColor get() = JavaColor(r, g, b, a)

    /**
     * Used to tell the [rgba] value to update when the HSBA values are changed.
     *
     * @see rgba
     */
    private var needsUpdate = true

    /**
     * RGBA value from a color.
     *
     * Gets recolored when the HSBA values are changed.
     * @see needsUpdate
     */
    var rgba: Int = 0
        get() {
            return if (needsUpdate) {
                HSBtoRGB(hue, saturation, brightness).let { rgb ->
                    (rgb and 0x00FFFFFF) or ((alpha * 255).toInt() shl 24)
                }.also {
                    needsUpdate = false
                    field = it
                }
            } else {
                field
            }
        }

    inline val r get() = rgba.red
    inline val g get() = rgba.green
    inline val b get() = rgba.blue
    inline val a get() = rgba.alpha

    /**
     * Checks if color isn't visible.
     * Main use is to prevent rendering when the color is invisible.
     */
    inline val isTransparent: Boolean
        get() = alpha == 0f

    override fun toString(): String = "Color(red=$r,green=$g,blue=$b,alpha=$a)"

    override fun hashCode(): Int {
        var result = hue.toInt()
        result = 31 * result + saturation.toInt()
        result = 31 * result + brightness.toInt()
        result = 31 * result + alpha.toInt()
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other is Color) {
            return rgba == other.rgba
        }
        return false
    }

    companion object {

        @JvmField
        val TRANSPARENT = Color(0, 0, 0, 0f)

        @JvmField
        val WHITE = Color(255, 255, 255)

        @JvmField
        val BLACK = Color(0, 0, 0)

        @JvmField
        val PURPLE = Color(170, 0, 170)

        @JvmField
        val ORANGE = Color(255, 170, 0)

        @JvmField
        val GREEN = Color(0, 255, 0)

        @JvmField
        val DARK_GREEN = Color(0, 170, 0)

        @JvmField
        val DARK_RED = Color(170, 0, 0)

        @JvmField
        val RED = Color(255, 0, 0)

        @JvmField
        val GRAY = Color(170, 170, 170)

        @JvmField
        val DARK_GRAY = Color(35, 35, 35)

        @JvmField
        val BLUE = Color(85,  255,255)

        @JvmField
        val PINK = Color(255,85,255)

        @JvmField
        val YELLOW = Color(255, 255, 85)

        @JvmField
        val CYAN = Color(0, 170, 170)
       
        inline val Int.red get() = this shr 16 and 0xFF
        inline val Int.green get() = this shr 8 and 0xFF
        inline val Int.blue get() = this and 0xFF
        inline val Int.alpha get() = this shr 24 and 0xFF

    }
}
