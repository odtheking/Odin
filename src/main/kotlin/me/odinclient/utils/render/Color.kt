package me.odinclient.utils.render

import me.odinclient.utils.Utils.HSBtoRGB
import me.odinclient.utils.Utils.RGBtoHSB

// TODO: Clean up all color related stuff etc
/**
 * Taken from [PolyUI](https://github.com/Polyfrost/polyui-jvm/blob/master/src/main/kotlin/cc/polyfrost/polyui/color/Color.kt)
 */
class Color(hue: Float, saturation: Float, brightness: Float, alpha: Float = 1f) {

    /**
     * Prints Color's RGBA values.
     */
    override fun toString(): String {
        return "Color(red=$r,green=$g,blue=$b,alpha=$a)"
    }

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

    // need to remove this cuz defeats whole purpose of this class
    // (to make getting colors from hsba and converting it to rgba without needing to do it more than once)
    val javaColor get() = java.awt.Color(r, g, b, a)

    private var needsUpdate = true // this updates argb

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

    val isTransparent: Boolean
        get() = alpha == 0f

    companion object {

        @JvmField
        val TRANSPARENT = Color(0, 0, 0, 0f)

        val WHITE = Color(255, 255, 255)

        inline val Int.red get() = this shr 16 and 0xFF
        inline val Int.green get() = this shr 8 and 0xFF
        inline val Int.blue get() = this and 0xFF
        inline val Int.alpha get() = this shr 24 and 0xFF
    }
}