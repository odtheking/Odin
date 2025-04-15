package me.odinmain.utils.render

import com.google.gson.*
import me.odinmain.utils.ui.Colors
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

    var alphaFloat = alpha
        set(value) {
            field = value
            needsUpdate = true
        }

    // Only used in Window, because that rendering needs java.awt.Color
    val javaColor get() = JavaColor(red, green, blue, alpha)

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
                field = (HSBtoRGB(hue, saturation, brightness) and 0X00FFFFFF) or ((this.alphaFloat * 255).toInt() shl 24)
                needsUpdate = false
            }
            return field
        }

    inline val red get() = rgba.red
    inline val green get() = rgba.green
    inline val blue get() = rgba.blue
    inline val alpha get() = rgba.alpha

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
        get() = this.alphaFloat == 0f

    override fun toString(): String = "Color(red=$red,green=$green,blue=$blue,alpha=$alpha)"

    override fun hashCode(): Int {
        var result = hue.toInt()
        result = 31 * result + saturation.toInt()
        result = 31 * result + brightness.toInt()
        result = 31 * result + this.alphaFloat.toInt()
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
        inline val Int.red get() = this shr 16 and 0xFF
        inline val Int.green get() = this shr 8 and 0xFF
        inline val Int.blue get() = this and 0xFF
        inline val Int.alpha get() = this shr 24 and 0xFF
    }

    class ColorSerializer : JsonSerializer<Color>, JsonDeserializer<Color> {
        override fun serialize(src: Color?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
            return JsonPrimitive("#${src?.hex ?: Colors.BLACK.hex}")
        }

        override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): Color {
            val hexValue = json?.asString?.replace("#", "") ?: "00000000"
            return Color(hexValue)
        }
    }
}
