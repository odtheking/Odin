@file:JvmName("Utils")

package me.odinmain.utils

import me.odinmain.OdinMain
import me.odinmain.OdinMain.logger
import me.odinmain.OdinMain.mc
import me.odinmain.features.ModuleManager
import me.odinmain.utils.skyblock.modMessage
import me.odinmain.utils.skyblock.sendCommand
import me.odinmain.utils.skyblock.skyblockID
import net.minecraft.client.gui.GuiScreen
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.SharedMonsterAttributes
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.event.ClickEvent
import net.minecraft.event.HoverEvent
import net.minecraft.inventory.Container
import net.minecraft.inventory.ContainerChest
import net.minecraft.util.ChatComponentText
import net.minecraft.util.ChatStyle
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.eventhandler.Event
import java.util.*
import kotlin.math.pow
import kotlin.math.round

val FORMATTING_CODE_PATTERN = Regex("§[0-9a-fk-or]", RegexOption.IGNORE_CASE)

/**
 * Returns the string without any minecraft formatting codes.
 */
inline val String?.noControlCodes: String
    get() = this?.replace(FORMATTING_CODE_PATTERN, "") ?: ""

/**
 * Checks if the current string contains at least one of the specified strings.
 *
 * @param options List of strings to check.
 * @param ignoreCase If comparison should be case-sensitive or not.
 * @return `true` if the string contains at least one of the specified options, otherwise `false`.
 */
fun String.containsOneOf(vararg options: String, ignoreCase: Boolean = false): Boolean =
    containsOneOf(options.toList(), ignoreCase)

/**
 * Checks if the current string contains at least one of the specified strings.
 *
 * @param options List of strings to check.
 * @param ignoreCase If comparison should be case-sensitive or not.
 * @return `true` if the string contains at least one of the specified options, otherwise `false`.
 */
fun String.containsOneOf(options: Collection<String>, ignoreCase: Boolean = false): Boolean =
    options.any { this.contains(it, ignoreCase) }

fun Number.toFixed(decimals: Int = 2): String =
    "%.${decimals}f".format(Locale.US, this)

fun String.startsWithOneOf(vararg options: String, ignoreCase: Boolean = false): Boolean =
    options.any { this.startsWith(it, ignoreCase) }

/**
 * Checks if the current object is equal to at least one of the specified objects.
 *
 * @param options List of other objects to check.
 * @return `true` if the object is equal to one of the specified objects.
 */
fun Any?.equalsOneOf(vararg options: Any?): Boolean =
    options.any { this == it }

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

inline val ContainerChest.name: String
    get() = this.lowerChestInventory?.displayName?.unformattedText ?: ""

inline val Container.name: String
    get() = (this as? ContainerChest)?.name ?: "Undefined Container"

operator fun Number.div(number: Number): Number =
    this.toDouble() / number.toDouble()

operator fun Number.times(number: Number): Number =
    this.toDouble() * number.toDouble()

operator fun Number.minus(number: Number): Number =
    this.toDouble() - number.toDouble()

operator fun Number.plus(number: Number): Number =
    this.toDouble() + number.toDouble()

/**
 * Posts an event to the event bus and catches any errors.
 * @author Skytils
 */
fun Event.postAndCatch(): Boolean =
    runCatching {
        MinecraftForge.EVENT_BUS.post(this)
    }.onFailure {
        logError(it, this)
    }.getOrDefault(isCanceled)

fun logError(throwable: Throwable, context: Any) {
    val message = "${OdinMain.VERSION} Caught an ${throwable::class.simpleName ?: "error"} at ${context::class.simpleName}."
    logger.error(message, throwable)
    val style = ChatStyle().apply {
        chatClickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, "/od copy $message \n``` ${throwable.message} \n${throwable.stackTraceToString().lineSequence().take(10).joinToString("\n")}```")
        chatHoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, ChatComponentText("§6Click to copy the error to your clipboard."))
    }
    modMessage("$message §cPlease click this message to copy and send it in the Odin discord!", chatStyle = style)
}

/**
 * Executes the specified function after the specified number of **minecraft** ticks.
 * @param ticks The number of ticks to wait.
 * @param func The function to execute after the specified number of
 */
fun runIn(ticks: Int, server: Boolean = false, func: () -> Unit) {
    if (ticks <= 0)
        return func()
    ModuleManager.tickTasks.add(ModuleManager.TickTask(ticks, server, func))
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
fun startProfile(name: String) =
    mc.mcProfiler.startSection("Odin: $name")

/**
 * Ends the current minecraft profiler section.
 */
fun endProfile() =
    mc.mcProfiler.endSection()

/**
 * Returns the String with the first letter capitalized
 *
 * @return The String with the first letter capitalized
 */
fun String.capitalizeFirst(): String = replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

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
    return "$hours$minutes${(remaining / 1000f).toFixed(decimalPlaces)}s"
}

inline val Char.isHexaDecimal
    get() = isDigit() || lowercase().equalsOneOf("a","b","c","d","e","f")

/**
 * Writes the given text to the clipboard.
 */
fun writeToClipboard(text: String, successMessage: String = "§aCopied to clipboard.") {
    GuiScreen.setClipboardString(text)
    if (successMessage.isNotEmpty()) modMessage(successMessage)
}

private val romanMap = mapOf('I' to 1, 'V' to 5, 'X' to 10, 'L' to 50, 'C' to 100, 'D' to 500, 'M' to 1000)
private val numberRegex = Regex("^[0-9]+$")
fun romanToInt(s: String): Int {
    return if (s.matches(numberRegex)) s.toInt()
    else {
        var result = 0
        for (i in 0 until s.length - 1) {
            val current = romanMap[s[i]] ?: 0
            val next = romanMap[s[i + 1]] ?: 0
            result += if (current < next) -current else current
        }
        result + (romanMap[s.last()] ?: 0)
    }
}

fun fillItemFromSack(amount: Int, itemId: String, sackName: String, sendMessage: Boolean) {
    val needed = mc.thePlayer?.inventory?.mainInventory?.find { it?.skyblockID == itemId }?.stackSize ?: 0
    if (needed != amount) sendCommand("gfs $sackName ${amount - needed}") else if (sendMessage) modMessage("§cAlready at max stack size.")
}

inline fun <T> MutableCollection<T>.removeFirstOrNull(predicate: (T) -> Boolean): T? {
    val first = firstOrNull(predicate) ?: return null
    this.remove(first)
    return first
}

fun Int.addRange(add: Int): IntRange = this..this+add

fun runOnMCThread(run: () -> Unit) {
    if (!mc.isCallingFromMinecraftThread) mc.addScheduledTask(run) else run()
}

fun EntityPlayer?.isOtherPlayer(): Boolean {
    return this != null && this != mc.thePlayer && this.uniqueID.version() != 2
}

fun EntityLivingBase?.getSBMaxHealth(): Float {
    return this?.getEntityAttribute(SharedMonsterAttributes.maxHealth)?.baseValue?.toFloat() ?: 0f
}