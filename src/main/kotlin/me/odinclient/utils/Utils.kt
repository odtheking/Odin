package me.odinclient.utils

import net.minecraft.inventory.ContainerChest
import kotlin.math.floor

object Utils {

    private val FORMATTING_CODE_PATTERN = Regex("§[0-9a-fk-or]", RegexOption.IGNORE_CASE)

    val String?.noControlCodes: String get() = this?.let { FORMATTING_CODE_PATTERN.replace(it, "") } ?: ""

    @Deprecated("This is useless", replaceWith = ReplaceWith("coerceIn"))
    fun Number.clamp(min: Number = 0F, max: Number = 1F): Float =
        if (this.toFloat() < min.toFloat()) min.toFloat() else this.toFloat().coerceAtMost(max.toFloat())

    @Deprecated("This is useless", replaceWith = ReplaceWith("coerceIn"))
    fun Int.clamp(min: Int, max: Int): Int =
        if (this < min) min else this.coerceAtMost(max)

    fun String.containsOneOf(vararg options: String, ignoreCase: Boolean = false): Boolean {
        for (i in options.indices) if (this.contains(options[i], ignoreCase)) return true
        return false
    }

    fun String.containsOneOf(options: List<String>, ignoreCase: Boolean = false): Boolean {
        for (i in options.indices) if (this.contains(options[i], ignoreCase)) return true
        return false
    }

    fun Any?.equalsOneOf(vararg other: Any): Boolean {
        return other.any {
            this == it
        }
    }

    fun Double.floor(): Double {
        return kotlin.math.floor(this)
    }

    fun Double.floorToInt(): Int {
        return kotlin.math.floor(this).toInt()
    }

    val ContainerChest.name: String
        get() = this.lowerChestInventory.displayName.unformattedText



    operator fun Number.div(number: Number): Number {
        return this.toDouble() / number.toDouble()
    }

    operator fun Number.times(number: Number): Number {
        return this.toDouble() * number.toDouble()
    }

    operator fun Number.minus(number: Number): Number {
        return this.toDouble() - number.toDouble()
    }

    operator fun Number.plus(number: Number): Number {
        return this.toDouble() + number.toDouble()
    }

    operator fun Number.unaryMinus(): Number {
        return -this.toDouble()
    }

    operator fun Number.compareTo(number: Number): Int {
        return this.toDouble().compareTo(number.toDouble())
    }

    fun Number.coerceInNumber(min: Number, max: Number): Number {
        if (this < min) return min
        if (this > max) return max
        return this
    }

    /**
     * Taken from PolyUI
     */
    @Suppress("FunctionName")
    fun HSBtoRGB(hue: Float, saturation: Float, brightness: Float): Int {
        var r = 0
        var g = 0
        var b = 0
        if (saturation == 0f) {
            b = (brightness * 255.0f + 0.5f).toInt()
            g = b
            r = g
        } else {
            val h = (hue - floor(hue)) * 6.0f
            val f = h - floor(h)
            val p = brightness * (1.0f - saturation)
            val q = brightness * (1.0f - saturation * f)
            val t = brightness * (1.0f - saturation * (1.0f - f))
            when (h.toInt()) {
                0 -> {
                    r = (brightness * 255.0f + 0.5f).toInt()
                    g = (t * 255.0f + 0.5f).toInt()
                    b = (p * 255.0f + 0.5f).toInt()
                }

                1 -> {
                    r = (q * 255.0f + 0.5f).toInt()
                    g = (brightness * 255.0f + 0.5f).toInt()
                    b = (p * 255.0f + 0.5f).toInt()
                }

                2 -> {
                    r = (p * 255.0f + 0.5f).toInt()
                    g = (brightness * 255.0f + 0.5f).toInt()
                    b = (t * 255.0f + 0.5f).toInt()
                }

                3 -> {
                    r = (p * 255.0f + 0.5f).toInt()
                    g = (q * 255.0f + 0.5f).toInt()
                    b = (brightness * 255.0f + 0.5f).toInt()
                }

                4 -> {
                    r = (t * 255.0f + 0.5f).toInt()
                    g = (p * 255.0f + 0.5f).toInt()
                    b = (brightness * 255.0f + 0.5f).toInt()
                }

                5 -> {
                    r = (brightness * 255.0f + 0.5f).toInt()
                    g = (p * 255.0f + 0.5f).toInt()
                    b = (q * 255.0f + 0.5f).toInt()
                }
            }
        }
        return -0x1000000 or (r shl 16) or (g shl 8) or (b shl 0)
    }

    /**
     * Taken from PolyUI
     */
    @Suppress("FunctionName", "NAME_SHADOWING")
    fun RGBtoHSB(r: Int, g: Int, b: Int, out: FloatArray? = null): FloatArray {
        var hue: Float
        val saturation: Float
        val brightness: Float

        val out = out ?: FloatArray(3)

        var cmax = if (r > g) r else g
        if (b > cmax) cmax = b
        var cmin = if (r < g) r else g
        if (b < cmin) cmin = b

        brightness = cmax.toFloat() / 255.0f
        saturation = if (cmax != 0) (cmax - cmin).toFloat() / cmax.toFloat() else 0f
        if (saturation == 0f) {
            hue = 0f
        } else {
            val redc = (cmax - r).toFloat() / (cmax - cmin).toFloat()
            val greenc = (cmax - g).toFloat() / (cmax - cmin).toFloat()
            val bluec = (cmax - b).toFloat() / (cmax - cmin).toFloat()
            hue = if (r == cmax) bluec - greenc else if (g == cmax) 2.0f + redc - bluec else 4.0f + greenc - redc
            hue /= 6.0f
            if (hue < 0) hue += 1.0f
        }
        out[0] = hue
        out[1] = saturation
        out[2] = brightness
        return out
    }
}