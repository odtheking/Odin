package me.odinclient.utils.render

import me.odinclient.utils.Utils.HSBtoRGB
import me.odinclient.utils.Utils.RGBtoHSB

// TODO: Clean up all color related stuff etc
/**
 * Based on [PolyUI](https://github.com/Polyfrost/polyui-jvm/blob/master/src/main/kotlin/cc/polyfrost/polyui/color/Color.kt)
 */
class Color(hue: Float, saturation: Float, brightness: Float, alpha: Float = 1f) {

    constructor(hsb: FloatArray, alpha: Float = 1f) : this(hsb[0], hsb[1], hsb[2], alpha)
    constructor(r: Int, g: Int, b: Int, alpha: Float = 1f) : this(RGBtoHSB(r, g, b), alpha)

    constructor(rgba: Int) : this(rgba.red, rgba.green, rgba.blue, rgba.alpha / 255f)
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
    val javaColor get() = java.awt.Color(r, g, b, a)

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

    /**
     * Returns red from [rgba] value
     */
    inline val r
        get() = rgba.red

    /**
     * Returns green from [rgba] value
     */
    inline val g
        get() = rgba.green

    /**
     * Returns blue from [rgba] value
     */
    inline val b
        get() = rgba.blue

    /**
     * Returns opacity from [rgba] value
     */
    inline val a
        get() = rgba.alpha

    /**
     * Checks if color isn't visible.
     * Used for rendering to not waste resources rendering something when its invisible.
     */
    val isTransparent: Boolean
        get() = alpha == 0f

    /**
     * Prints Color's RGBA values.
     */
    override fun toString(): String =
        "Color(red=$r,green=$g,blue=$b,alpha=$a)"

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
        val GOLD = Color(255, 170, 0)

        /**
         * Performs bit-shift thingy
         */
        inline val Int.red
            get() = this shr 16 and 0xFF

        inline val Int.green
            get() = this shr 8 and 0xFF

        inline val Int.blue
            get() = this and 0xFF

        inline val Int.alpha
            get() = this shr 24 and 0xFF
    }
}