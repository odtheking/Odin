package com.odtheking.odin.utils

import com.google.gson.*
import com.google.gson.annotations.JsonAdapter
import net.minecraft.network.chat.TextColor
import net.minecraft.util.CommonColors
import java.awt.Color.HSBtoRGB
import java.awt.Color.RGBtoHSB
import java.lang.reflect.Type

@JsonAdapter(Color.ColorSerializer::class)
class Color private constructor(
    @JvmField val rgba: Int,
    @Volatile private var hsb: FloatArray?
) {
    constructor(hue: Float, saturation: Float, brightness: Float, alpha: Float = 1f) :
            this((HSBtoRGB(hue, saturation, brightness) and 0xFFFFFF) or alphaBits(alpha), floatArrayOf(hue, saturation, brightness))
    constructor(r: Int, g: Int, b: Int, alpha: Float = 1f) :
            this(alphaBits(alpha) or ((r and 0xFF) shl 16) or ((g and 0xFF) shl 8) or (b and 0xFF), null)
    constructor(rgba: Int) : this(rgba, null)
    constructor(rgba: Int, alpha: Float) : this((rgba and 0xFFFFFF) or alphaBits(alpha), null)

    // #RRGGBBAA
    constructor(hex: String) : this(hex.substring(0, 8).toLong(16).toInt().let { (it ushr 8) or (it shl 24) }, null)

    private fun hsb(): FloatArray = hsb ?: RGBtoHSB(red, green, blue, null).also { hsb = it }

    val hue: Float get() = hsb()[0]
    val saturation: Float get() = hsb()[1]
    val brightness: Float get() = hsb()[2]

    inline val red get() = rgba.red
    inline val green get() = rgba.green
    inline val blue get() = rgba.blue
    inline val alpha get() = rgba.alpha

    inline val redFloat get() = red / 255f
    inline val greenFloat get() = green / 255f
    inline val blueFloat get() = blue / 255f
    inline val alphaFloat get() = alpha / 255f

    // #RRGGBBAA || #RRGGBB
    fun hex(includeAlpha: Boolean = true): String =
        if (includeAlpha) ((rgba shl 8) or alpha).toHexString(HexFormat.UpperCase)
        else rgba.toHexString(HexFormat.UpperCase).substring(2)

    inline val isTransparent: Boolean
        get() = alpha == 0

    override fun toString(): String = "Color(red=$red,green=$green,blue=$blue,alpha=$alpha)"

    override fun hashCode(): Int = rgba

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is Color || rgba != other.rgba) return false
        val a = hsb
        val b = other.hsb
        return a === b || (a ?: hsb()).contentEquals(b ?: other.hsb())
    }

    private class ColorSerializer : JsonDeserializer<Color>, JsonSerializer<Color> {
        override fun deserialize(json: JsonElement, type: Type, context: JsonDeserializationContext?): Color {
            return Color(json.asString.drop(1))
        }

        override fun serialize(color: Color, type: Type, context: JsonSerializationContext?): JsonElement {
            return JsonPrimitive("#${color.hex()}")
        }
    }

    companion object {
        inline val Int.red get() = this shr 16 and 0xFF
        inline val Int.green get() = this shr 8 and 0xFF
        inline val Int.blue get() = this and 0xFF
        inline val Int.alpha get() = this shr 24 and 0xFF

        private fun alphaBits(alpha: Float): Int = (alpha.coerceIn(0f, 1f) * 255f).toInt() shl 24

        fun Color.brighter(factor: Float = 1.3f): Color =
            Color(hue, saturation, (brightness * factor.coerceAtLeast(1f)).coerceAtMost(1f), alphaFloat)

        fun Color.darker(factor: Float = 0.7f): Color =
            Color(hue, saturation, brightness * factor, alphaFloat)

        fun Color.withAlpha(alpha: Float): Color =
            Color((rgba and 0xFFFFFF) or alphaBits(alpha), hsb)

        fun Color.multiplyAlpha(factor: Float): Color = withAlpha(alphaFloat * factor)

        fun Color.hsbMax(): Color = Color(hue, 1f, 1f)
    }
}

object Colors {

    @JvmField val MINECRAFT_DARK_BLUE = Color(TextColor.DARK_BLUE.value, 1f)
    @JvmField val MINECRAFT_DARK_GREEN = Color(TextColor.DARK_GREEN.value, 1f)
    @JvmField val MINECRAFT_DARK_AQUA = Color(TextColor.DARK_AQUA.value, 1f)
    @JvmField val MINECRAFT_DARK_RED = Color(TextColor.DARK_RED.value, 1f)
    @JvmField val MINECRAFT_DARK_PURPLE = Color(TextColor.DARK_PURPLE.value, 1f)
    @JvmField val MINECRAFT_GOLD = Color(TextColor.GOLD.value, 1f)
    @JvmField val MINECRAFT_GRAY = Color(TextColor.GRAY.value, 1f)
    @JvmField val MINECRAFT_DARK_GRAY = Color(TextColor.DARK_GRAY.value, 1f)
    @JvmField val MINECRAFT_BLUE = Color(TextColor.BLUE.value, 1f)
    @JvmField val MINECRAFT_GREEN = Color(TextColor.GREEN.value, 1f)
    @JvmField val MINECRAFT_AQUA = Color(TextColor.AQUA.value, 1f)
    @JvmField val MINECRAFT_RED = Color(TextColor.RED.value, 1f)
    @JvmField val MINECRAFT_LIGHT_PURPLE = Color(TextColor.LIGHT_PURPLE.value, 1f)
    @JvmField val MINECRAFT_YELLOW = Color(TextColor.YELLOW.value, 1f)
    @JvmField val WHITE = Color(CommonColors.WHITE)
    @JvmField val BLACK = Color(CommonColors.BLACK)
    @JvmField val TRANSPARENT = Color(0)

    val minecraftColors = listOf(
        MINECRAFT_DARK_BLUE, MINECRAFT_DARK_GREEN, MINECRAFT_DARK_AQUA, MINECRAFT_DARK_RED,
        MINECRAFT_DARK_PURPLE, MINECRAFT_GOLD, MINECRAFT_GRAY, MINECRAFT_DARK_GRAY,
        MINECRAFT_BLUE, MINECRAFT_GREEN, MINECRAFT_AQUA, MINECRAFT_RED,
        MINECRAFT_LIGHT_PURPLE, MINECRAFT_YELLOW, WHITE
    )

    @JvmField val gray38 = Color(38, 38, 38)
    @JvmField val gray26 = Color(26, 26, 26)
}