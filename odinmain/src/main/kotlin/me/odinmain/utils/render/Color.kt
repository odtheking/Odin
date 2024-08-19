package me.odinmain.utils.render

import com.google.gson.*
import java.awt.Color.HSBtoRGB
import java.awt.Color.RGBtoHSB
import java.lang.reflect.Type
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
    constructor(hex: String) : this(
        hex.substring(0, 2).toInt(16),
        hex.substring(2, 4).toInt(16),
        hex.substring(4, 6).toInt(16),
        hex.substring(6, 8).toInt(16) / 255f
    )

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
            if (needsUpdate) {
                field = (HSBtoRGB(hue, saturation, brightness) and 0X00FFFFFF) or ((alpha * 255).toInt() shl 24)
                needsUpdate = false
            }
            return field
        }

    inline val r get() = rgba.red
    inline val g get() = rgba.green
    inline val b get() = rgba.blue
    inline val a get() = rgba.alpha

    inline val redFloat get() = r / 255f
    inline val greenFloat get() = g / 255f
    inline val blueFloat get() = b / 255f
    inline val alphaFloat get() = this.alpha

    @OptIn(ExperimentalStdlibApi::class)
    val hex: String get() {
        return with(rgba.toHexString(HexFormat.UpperCase)) {
            return@with substring(2) + substring(0, 2)
        }
    }

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

    fun copy(): Color = Color(this.rgba)

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
        val YELLOW = Color(253, 218, 13)

        @JvmField
        val CYAN = Color(0, 170, 170)

        @JvmField
        val MAGENTA = Color(170, 0, 170)
       
        inline val Int.red get() = this shr 16 and 0xFF
        inline val Int.green get() = this shr 8 and 0xFF
        inline val Int.blue get() = this and 0xFF
        inline val Int.alpha get() = this shr 24 and 0xFF

    }

    class ColorSerializer : JsonSerializer<Color>, JsonDeserializer<Color> {
        override fun serialize(src: Color?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
            return JsonPrimitive("#${src?.hex ?: BLACK.hex}")
        }

        override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): Color {
            val hexValue = json?.asString?.replace("#", "") ?: "00000000"
            return Color(hexValue)
        }
    }
}
