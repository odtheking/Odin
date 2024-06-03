@file:Suppress("NOTHING_TO_INLINE", "FunctionName")

package me.odinmain.utils

import me.odinmain.OdinMain.mc
import me.odinmain.features.ModuleManager
import me.odinmain.ui.clickgui.util.ColorUtil.withAlpha
import me.odinmain.utils.render.Color
import me.odinmain.utils.skyblock.modMessage
import net.minecraft.client.Minecraft
import net.minecraft.inventory.Container
import net.minecraft.inventory.ContainerChest
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.eventhandler.Event
import org.lwjgl.input.Keyboard
import org.lwjgl.opengl.GL11
import org.lwjgl.util.glu.GLU
import java.awt.Toolkit
import java.awt.datatransfer.*
import java.time.Month
import java.time.format.TextStyle
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
inline fun profile(name: String, func: () -> Unit) {
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

fun getCurrentMonthName(): String {
    val currentMonth = Month.entries[java.time.LocalDateTime.now().monthValue - 1]
    return currentMonth.getDisplayName(TextStyle.FULL, Locale.getDefault())
}

fun formatTime(time: Long): String {
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
        "%.2f".format(it)
    }
    return "$hours$minutes${seconds}s"
}

val Char.isHexaDecimal
    get() = isDigit() || equalsOneOf("a","b","c","d","e","f","A","B","C","D","E","F")

data object FlareTextures {
    val warningFlareTexture = "ewogICJ0aW1lc3RhbXAiIDogMTY0NjY4NzMwNjIyMywKICAicHJvZmlsZUlkIiA6ICI0MWQzYWJjMmQ3NDk0MDBjOTA5MGQ1NDM0ZDAzODMxYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJNZWdha2xvb24iLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjJlMmJmNmMxZWMzMzAyNDc5MjdiYTYzNDc5ZTU4NzJhYzY2YjA2OTAzYzg2YzgyYjUyZGFjOWYxYzk3MTQ1OCIKICAgIH0KICB9Cn0="
    val alertFlareTexture = "ewogICJ0aW1lc3RhbXAiIDogMTY0NjY4NzMyNjQzMiwKICAicHJvZmlsZUlkIiA6ICI0MWQzYWJjMmQ3NDk0MDBjOTA5MGQ1NDM0ZDAzODMxYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJNZWdha2xvb24iLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWQyYmY5ODY0NzIwZDg3ZmQwNmI4NGVmYTgwYjc5NWM0OGVkNTM5YjE2NTIzYzNiMWYxOTkwYjQwYzAwM2Y2YiIKICAgIH0KICB9Cn0="
    val sosFlareTexture = "ewogICJ0aW1lc3RhbXAiIDogMTY0NjY4NzM0NzQ4OSwKICAicHJvZmlsZUlkIiA6ICI0MWQzYWJjMmQ3NDk0MDBjOTA5MGQ1NDM0ZDAzODMxYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJNZWdha2xvb24iLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzAwNjJjYzk4ZWJkYTcyYTZhNGI4OTc4M2FkY2VmMjgxNWI0ODNhMDFkNzNlYTg3YjNkZjc2MDcyYTg5ZDEzYiIKICAgIH0KICB9Cn0="
}

fun checkGLError(message: String) {
    var i: Int
    if ((GL11.glGetError().also { i = it }) != 0) {
        val s = GLU.gluErrorString(i)
        println("########## GL ERROR ##########")
        println("@ $message")
        println("$i: $s")
    }
}

fun copyToClipboard(text: String) {
    val clipboard = Toolkit.getDefaultToolkit().systemClipboard
    val stringSelection = StringSelection(text)
    clipboard.setContents(stringSelection, null)
}

fun getClipboardString(): String {
    try {
        val transferable = Toolkit.getDefaultToolkit().systemClipboard.getContents(null as Any?)
        if (transferable != null && transferable.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            return transferable.getTransferData(DataFlavor.stringFlavor) as String
        }
    } catch (_: java.lang.Exception) {
    }

    return ""
}

fun isCtrlKeyDown(): Boolean {
    return if (Minecraft.isRunningOnMac) Keyboard.isKeyDown(219) || Keyboard.isKeyDown(220) else Keyboard.isKeyDown(
        29
    ) || Keyboard.isKeyDown(157)
}

fun isShiftKeyDown(): Boolean {
    return Keyboard.isKeyDown(42) || Keyboard.isKeyDown(54)
}

private fun isAltKeyDown(): Boolean {
    return Keyboard.isKeyDown(56) || Keyboard.isKeyDown(184)
}

fun isKeyComboCtrlX(keyID: Int): Boolean {
    return keyID == 45 && isCtrlKeyDown() && !isShiftKeyDown() && !isAltKeyDown()
}

fun isKeyComboCtrlV(keyID: Int): Boolean {
    return keyID == 47 && isCtrlKeyDown() && !isShiftKeyDown() && !isAltKeyDown()
}

fun isKeyComboCtrlC(keyID: Int): Boolean {
    return keyID == 46 && isCtrlKeyDown() && !isShiftKeyDown() && !isAltKeyDown()
}

fun isKeyComboCtrlA(keyID: Int): Boolean {
    return keyID == 30 && isCtrlKeyDown() && !isShiftKeyDown() && !isAltKeyDown()
}