package com.odtheking.odin.commands

import com.github.stivais.commodore.Commodore
import com.github.stivais.commodore.utils.GreedyString
import com.github.stivais.commodore.utils.SyntaxException
import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.features.ModuleManager
import com.odtheking.odin.features.impl.dungeon.PositionalMessages
import com.odtheking.odin.features.impl.dungeon.PositionalMessages.posMessageStrings
import com.odtheking.odin.utils.Color
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.modMessage
import kotlin.math.floor

val posMsgCommand = Commodore("posmsg") {
    literal("add") {
        literal("at").executable {
            param("x") {
                suggests { listOfNotNull(mc.player?.x?.let { floor(it).toString() }) }
            }
            param("y") {
                suggests { listOfNotNull(mc.player?.y?.let { floor(it).toString() }) }
            }
            param("z") {
                suggests { listOfNotNull(mc.player?.z?.let { floor(it).toString() }) }
            }
            param("color") {
                suggests {
                    listOf("darkblue", "darkgreen", "darkaqua", "darkred", "darkpurple",
                        "gold", "gray", "darkgray", "blue", "green", "aqua", "red",
                        "lightpurple", "yellow", "white", "black")
                }
            }

            runs { x: Double, y: Double, z: Double, delay: Int, distance: Double, color: String, message: GreedyString? ->
                if (posMessageStrings.any { it.message == message?.string }) return@runs
                val color = getColorFromString(color) ?: return@runs modMessage("Unknown color $color")
                posMessageStrings.add(PositionalMessages.PosMessage(x, y, z, null, null, null, delay, distance, color, message?.string))
                modMessage("Message \"${message}\" added at $x, $y, $z, with ${delay}t delay, triggered up to $distance blocks away.")
                ModuleManager.saveConfigurations()
            }
        }

        literal("in").executable {
            param("color") {
                suggests {
                    listOf("darkblue", "darkgreen", "darkaqua", "darkred", "darkpurple",
                        "gold", "gray", "darkgray", "blue", "green", "aqua", "red",
                        "lightpurple", "yellow", "white", "black")
                }
            }

            runs { x: Double, y: Double, z: Double, x2: Double, y2: Double, z2: Double, delay: Int, color: String, message: GreedyString? ->
                if (posMessageStrings.any { it.message == message?.string }) return@runs
                val color = getColorFromString(color) ?: return@runs modMessage("Unknown color $color")
                posMessageStrings.add(PositionalMessages.PosMessage(x, y, z, x2, y2, z2, delay, null, color, message?.string))
                modMessage("Message \"${message}\" added in $x, $y, $z, $x2, $y2, $z2, with ${delay}t delay.")
                ModuleManager.saveConfigurations()
            }
        }
    }

    literal("remove").executable {
        param("message") {
            parser { greedy: GreedyString ->
                val input = greedy.string.trim()
                val found = posMessageStrings.any { it.message.equals(input, true) }
                if (!found) throw SyntaxException("Message not found. Available messages: ${posMessageStrings.joinToString { "\"${it.message}\"" }}")
                input
            }
            suggests {
                posMessageStrings.mapNotNull { it.message }.distinct()
            }
        }

        runs { message: String ->
            val removed = posMessageStrings.filter { it.message.equals(message, true) }
            posMessageStrings.removeAll { it.message.equals(message, true) }
            modMessage("Removed ${removed.size} Positional Message(s): \"$message\"")
            ModuleManager.saveConfigurations()
        }
    }

    literal("clear").runs {
        modMessage("Cleared List")
        posMessageStrings.clear()
        ModuleManager.saveConfigurations()
    }

    literal("list").runs {
        val output = posMessageStrings.joinToString(separator = "\n") {
            "${posMessageStrings.indexOf(it) + 1}: ${it.x}, ${it.y}, ${it.z}, ${it.x2}, ${it.y2}, ${it.z2}, ${it.delay}, ${it.distance}, ${it.color.hex()}, \"${it.message}\""
        }
        modMessage(if (posMessageStrings.isEmpty()) "Positional Message list is empty!" else "Positonal Message list:\n$output")
    }
}

fun getColorFromString(color: String): Color? {
    return when (color.uppercase()) {
        "DARKBLUE" -> Colors.MINECRAFT_DARK_BLUE
        "DARKGREEN" -> Colors.MINECRAFT_DARK_GREEN
        "DARKAQUA" -> Colors.MINECRAFT_DARK_AQUA
        "DARKRED" -> Colors.MINECRAFT_DARK_RED
        "DARKPURPLE" -> Colors.MINECRAFT_DARK_PURPLE
        "GOLD" -> Colors.MINECRAFT_GOLD
        "GRAY" -> Colors.MINECRAFT_GRAY
        "DARKGRAY" -> Colors.MINECRAFT_DARK_GRAY
        "BLUE" -> Colors.MINECRAFT_BLUE
        "GREEN" -> Colors.MINECRAFT_GREEN
        "AQUA" -> Colors.MINECRAFT_AQUA
        "RED" -> Colors.MINECRAFT_RED
        "LIGHTPURPLE" -> Colors.MINECRAFT_LIGHT_PURPLE
        "YELLOW" -> Colors.MINECRAFT_YELLOW
        "WHITE" -> Colors.WHITE
        "BLACK" -> Colors.BLACK
        else -> null
    }
}