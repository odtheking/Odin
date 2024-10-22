@file:Suppress("FunctionName")
@file:JvmName("Utils")

package me.odinmain.utils

import me.odinmain.OdinMain
import me.odinmain.OdinMain.logger
import me.odinmain.OdinMain.mc
import me.odinmain.features.ModuleManager
import me.odinmain.ui.clickgui.util.ColorUtil.withAlpha
import me.odinmain.utils.render.Color
import me.odinmain.utils.skyblock.*
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.event.ClickEvent
import net.minecraft.event.HoverEvent
import net.minecraft.inventory.Container
import net.minecraft.inventory.ContainerChest
import net.minecraft.util.ChatComponentText
import net.minecraft.util.ChatStyle
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.eventhandler.Event
import org.lwjgl.opengl.GL11
import org.lwjgl.util.glu.GLU
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.util.*
import kotlin.math.*

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
fun String.containsOneOf(options: Collection<String>, ignoreCase: Boolean = false): Boolean {
    return options.any { this.contains(it, ignoreCase) }
}

fun String.startsWithOneOf(vararg options: String, ignoreCase: Boolean = false): Boolean {
    return options.any { this.startsWith(it, ignoreCase) }
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

fun String?.matchesOneOf(vararg options: Regex): Boolean {
    return options.any { it.matches(this ?: "") }
}

/**
 * Checks if the first value in the pair equals the first argument and the second value in the pair equals the second argument.
 */
fun Pair<Any?, Any?>?.equal(first: Any?, second: Any?): Boolean {
    return this?.first == first && this?.second == second
}

/**
 * Floors the current Double number.
 * @return The floored Double number.
 */
fun Double.floor(): Double {
    return floor(this)
}

/**
 * Floors the current Float number.
 * @return The floored Float number.
 */
fun Float.floor(): Float {
    return floor(this.toDouble()).toFloat()
}

/**
 * Floors the current Long number.
 * @return The floored Long number (no change as Long is already an integer).
 */
fun Long.floor(): Long {
    return this
}

/**
 * Floors the current Int number.
 * @return The floored Int number (no change as Int is already an integer).
 */
fun Int.floor(): Int {
    return this
}

/**
 * Rounds the current number to the specified number of decimals.
 * @param decimals The number of decimals to round to.
 * @return The rounded number.
 */
fun Number.round(decimals: Int): Number {
    require(decimals >= 0) { "Decimals must be non-negative" }
    val factor = 10.0.pow(decimals)
    return round(this.toDouble() * factor) / factor
}

val ContainerChest.name: String
    get() = this.lowerChestInventory?.displayName?.unformattedText ?: ""

val Container.name: String
    get() = (this as? ContainerChest)?.name ?: "Undefined Container"

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
        logger.error("An error occurred", it)
        val style = ChatStyle()
        style.chatClickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, "/od copy ```${it.stackTraceToString().lineSequence().take(10).joinToString("\n")}```")
        style.chatHoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, ChatComponentText("§6Click to copy the error to your clipboard."))
        modMessage("${OdinMain.VERSION} Caught an ${it::class.simpleName ?: "error"} at ${this::class.simpleName}. §cPlease click this message to copy and send it in the Odin discord!",
            chatStyle = style)}.getOrDefault(isCanceled)
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
inline fun profile(name: String, func: () -> Unit) {
    startProfile(name)
    func()
    endProfile()
}

/**
 * Starts a minecraft profiler section with the specified name + "Odin: ".
 * */
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

/**
 * Formats a time duration in milliseconds into a human-readable string.
 *
 * The string will show hours, minutes, and seconds, with an optional number of decimal places for the seconds.
 *
 * @param time The time duration in milliseconds to be formatted.
 * @param decimalPlaces The number of decimal places to show for the seconds. Default is 2.
 * @return A formatted string representing the time duration. For example, "1h 2m 3.45s".
 */
fun formatTime(time: Long, decimalPlaces: Int = 2): String {
    if (time == 0L) return "0s"
    var remaining = time
    val hours = (remaining / 3600000).toInt().let {
        remaining -= it * 3600000
        if (it > 0) "${it}h " else ""
    }
    val minutes = (remaining / 60000).toInt().let {
        remaining -= it * 60000
        if (it > 0) "${it}m " else ""
    }
    val seconds = (remaining / 1000f).let {
        String.format(Locale.US, "%.${decimalPlaces}f", it)
    }
    return "$hours$minutes${seconds}s"
}

val Char.isHexaDecimal
    get() = isDigit() || lowercase().equalsOneOf("a","b","c","d","e","f")

fun checkGLError(message: String) {
    var i: Int
    if ((GL11.glGetError().also { i = it }) != 0) {
        val s = GLU.gluErrorString(i)
        println("########## GL ERROR ##########")
        println("@ $message")
        println("$i: $s")
    }
}

/**
 * Writes the given text to the clipboard.
 */
fun writeToClipboard(text: String, successMessage: String?) {
    try {
        val clipboard = Toolkit.getDefaultToolkit().systemClipboard
        val stringSelection = StringSelection(text)
        clipboard.setContents(stringSelection, null)
        if (successMessage != null)
            modMessage(successMessage)
    } catch (_: Exception) {
        devMessage("Clipboard not available!")
    }
}

fun writeToClipboard(text: String) {
    writeToClipboard(text, null)
}

private val romanMap = mapOf('I' to 1, 'V' to 5, 'X' to 10, 'L' to 50, 'C' to 100, 'D' to 500, 'M' to 1000)
fun romanToInt(s: String): Int {
    var result = 0
    for (i in 0 until s.length - 1) {
        val current = romanMap[s[i]] ?: 0
        val next = romanMap[s[i + 1]] ?: 0
        result += if (current < next) -current else current
    }
    return result + (romanMap[s.last()] ?: 0)
}

fun fillItemFromSack(amount: Int, itemId: String, sackName: String, sendMessage: Boolean) {
    val needed = mc.thePlayer?.inventory?.mainInventory?.find { it?.itemID == itemId }?.stackSize ?: 0
    if (needed != amount) sendCommand("gfs $sackName ${amount - needed}") else if (sendMessage) modMessage("§cAlready at max stack size.")
}

inline fun <T> MutableCollection<T>.removeFirstOrNull(predicate: (T) -> Boolean): T? {
    val first = firstOrNull(predicate) ?: return null
    this.remove(first)
    return first
}

fun Int.rangeAdd(add: Int): IntRange = this..this+add

val Entity.rotation get() = Pair(rotationYaw, rotationPitch)

fun runOnMCThread(run: () -> Unit) {
    if (!mc.isCallingFromMinecraftThread) mc.addScheduledTask(run) else run()
}

fun EntityPlayer?.isOtherPlayer(): Boolean {
    return this != null && this != mc.thePlayer && this.uniqueID.version() != 2
}