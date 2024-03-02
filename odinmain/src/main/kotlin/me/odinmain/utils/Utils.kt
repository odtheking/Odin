@file:Suppress("NOTHING_TO_INLINE", "FunctionName")

package me.odinmain.utils

import me.odinmain.OdinMain.mc
import me.odinmain.features.ModuleManager
import me.odinmain.ui.clickgui.util.ColorUtil.withAlpha
import me.odinmain.utils.render.Color
import me.odinmain.utils.skyblock.modMessage
import net.minecraft.inventory.ContainerChest
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.eventhandler.Event
import java.util.*
import kotlin.math.floor
import kotlin.math.pow
import kotlin.math.round


private val FORMATTING_CODE_PATTERN = Regex("§[0-9a-fk-or]", RegexOption.IGNORE_CASE)

/**
 * Returns the string without any minecraft formatting codes.
 */
val String?.noControlCodes: String
    get() = this?.let { FORMATTING_CODE_PATTERN.replace(it, "") } ?: ""

/**
 * Checks if the current string contains at least one of the specified strings.
 *
 * @param options List of strings to check.
 * @param ignoreCase If comparison should be case-sensitive or not.
 * @return `true` if the string contains at least one of the specified options, otherwise `false`.
 */
fun String.containsOneOf(vararg options: String, ignoreCase: Boolean = false): Boolean {
    return options.any { this.contains(it, ignoreCase) }
}

/**
 * Checks if the current string contains at least one of the specified strings.
 *
 * @param options List of strings to check.
 * @param ignoreCase If comparison should be case-sensitive or not.
 * @return `true` if the string contains at least one of the specified options, otherwise `false`.
 */
fun String.containsOneOf(options: List<String>, ignoreCase: Boolean = false): Boolean {
    return options.any { this.contains(it, ignoreCase) }
}

/**
 * Checks if the current object is equal to at least one of the specified objects.
 *
 * @param options List of other objects to check.
 * @return `true` if the object is equal to one of the specified objects.
 */
fun Any?.equalsOneOf(vararg options: Any?): Boolean {
    return options.any { this == it }
}

/**
 * Checks if the first value in the pair equals the first argument and the second value in the pair equals the second argument.
 */
fun Pair<Any?, Any?>?.equal(first: Any?, second: Any?): Boolean {
    return this?.first == first && this?.second == second
}

/**
 * Floors the double.
 */
inline fun Double.floor(): Double = floor(this)

/**
 * Rounds the double to the specified number of decimal places.
 */
fun Double.round(decimals: Int): Double {
    val multiplier = 10.0.pow(decimals)
    return round(this * multiplier) / multiplier
}

/**
 * Rounds the float to the specified number of decimal places.
 */
fun Float.round(decimals: Int): Float {
    val multiplier = 10f.pow(decimals)
    return round(this * multiplier) / multiplier
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

/**
 * Returns a random number between the specified range.
 */
fun IntRange.getRandom(): Int {
    return this.toList().getRandom()
}

/**
 * Returns a random element from the specified collection.
 */
fun <T> Collection<T>.getRandom(): T {
    return this.elementAt((Math.random() * this.size).floor().toInt())
}

/**
 * Posts an event to the event bus and catches any errors.
 * @author Skytils
 */
fun Event.postAndCatch(): Boolean {
    return runCatching {
        MinecraftForge.EVENT_BUS.post(this)
    }.onFailure {
        it.printStackTrace()
        modMessage("Caught and logged an ${it::class.simpleName ?: "error"} at ${this::class.simpleName}. Please report this!")
    }.getOrDefault(isCanceled)
}

/**
 * Executes the specified function after the specified number of **minecraft** ticks.
 * @param ticks The number of ticks to wait.
 * @param func The function to execute after the specified number of
 */
fun runIn(ticks: Int, func: () -> Unit) {
    if (ticks <= 0) {
        func()
        return
    }
    ModuleManager.tickTasks.add(ModuleManager.TickTask(ticks, func))
}

/**
 * Profiles the specified function with the specified string as profile section name.
 * Uses the minecraft profiler.
 *
 * @param name The name of the profile section.
 * @param func The code to profile.
 */
fun profile(name: String, func: () -> Unit) {
    startProfile(name)
    func()
    endProfile()
}

/**
 * Starts a minecraft profiler section with the specified name + "Odin: ".
 */
fun startProfile(name: String) {
    mc.mcProfiler.startSection("Odin: $name")
}

/**
 * Ends the current minecraft profiler section.
 */
fun endProfile() {
    mc.mcProfiler.endSection()
}

/**
 * Returns the maximum value of the numbers you give in as a float
 *
 * @param numbers All the numbers you want to compare
 *
 * @returns The maximum value of the numbers, as a float
 */
fun max(vararg numbers: Number): Float {
    return numbers.maxBy { it.toFloat() }.toFloat()
}

/**
 * Returns the minimum value of the numbers you give in as a float
 *
 * @param numbers All the numbers you want to compare
 *
 * @returns The minimum value of the numbers, as a float
 */
fun min(vararg numbers: Number): Float {
    return numbers.minBy { it.toFloat() }.toFloat()
}

/**
 * Returns the String with the first letter capitalized
 *
 * @param String The String to capitalize
 *
 * @return The String with the first letter capitalized
 */
fun String.capitalizeFirst(): String {
    return substring(0, 1).uppercase(Locale.getDefault()) + substring(1, length).lowercase()
}

fun Color.coerceAlpha(min: Float, max: Float): Color {
    return if (this.alpha < min) this.withAlpha(min)
    else if (this.alpha > max) this.withAlpha(max)
    else this
}

fun <T> Collection<T>.getSafe(index: Int?): T? {
    return try {
        this.toList()[index ?: return null]
    } catch (_: Exception) {
        null
    }
}