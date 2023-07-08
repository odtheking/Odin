package me.odinclient.commands

import net.minecraft.command.CommandBase
import net.minecraft.command.ICommandSender

abstract class Command(
    private val name: String,
    private val alias: List<String>
) : CommandBase() {
    final override fun getCommandName() = name
    final override fun getCommandAliases() = alias
    final override fun getRequiredPermissionLevel() = 0
    final override fun getCommandUsage(sender: ICommandSender) = "/$name"
    final override fun processCommand(sender: ICommandSender, args: Array<String>) = executeCommand(args.onEach { it.lowercase() })

    abstract fun executeCommand(args: Array<String>)

    /**
     * onEach implementation so it doesn't return out String
     */
    private inline fun <T> Array<T>.onEach(action: (T) -> Unit): Array<T> = apply { for (element in this) action(element) }

    /**
     * Turns a part of an array to a string. This is to reduce creating a new array just to get the same result.
     */
    fun Array<String>.joinToString(startIndex: Int, endIndex: Int = this.size): String {
        if (startIndex < 0 || startIndex >= size || endIndex <= startIndex || endIndex > size) throw IndexOutOfBoundsException("Invalid range for array")

        val result = StringBuilder()
        for (i in startIndex until endIndex) {
            if (i > startIndex) result.append(" ")
            result.append(this[i])
        }
        return result.toString()
    }
}