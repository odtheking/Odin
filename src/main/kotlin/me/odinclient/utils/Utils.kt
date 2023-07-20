package me.odinclient.utils

object Utils {

    private val FORMATTING_CODE_PATTERN = Regex("§[0-9a-fk-or]", RegexOption.IGNORE_CASE)

    val String?.noControlCodes: String get() = this?.let { FORMATTING_CODE_PATTERN.replace(it, "") } ?: ""

    @Deprecated("This is useless", replaceWith = ReplaceWith("coerceIn"))
    fun Number.clamp(min: Number = 0F, max: Number = 1F): Float =
        if (this.toFloat() < min.toFloat()) min.toFloat() else this.toFloat().coerceAtMost(max.toFloat())

    fun Int.clamp(min: Int, max: Int): Int =
        if (this < min) min else this.coerceAtMost(max)

    fun String.containsOneOf(vararg options: String, ignoreCase: Boolean = false): Boolean {
        for (i in options) if (this.contains(i, ignoreCase)) return true
        return false
    }

    fun Double.floor() = kotlin.math.floor(this)
}