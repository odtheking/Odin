package me.odinclient.utils

import net.minecraft.inventory.ContainerChest

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

    fun Number.coerceIn(min: Number, max: Number): Number {
        if (this < min) return min
        if (this > max) return max
        return this
    }
}