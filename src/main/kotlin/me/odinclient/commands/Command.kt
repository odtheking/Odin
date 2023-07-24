package me.odinclient.commands

import net.minecraft.command.CommandBase
import net.minecraft.command.ICommandSender
import net.minecraft.util.BlockPos

@Deprecated("Old", ReplaceWith("AbstractCommand", "me.odinclient.commands.AbstractCommand"))
abstract class Command(
    val name: String,
    private val alias: List<String>,
    val description: String
) : CommandBase() {
    final override fun getCommandName() = name
    final override fun getCommandAliases() = alias
    final override fun getRequiredPermissionLevel() = 0
    final override fun getCommandUsage(sender: ICommandSender) = "/$name"
    final override fun processCommand(sender: ICommandSender, args: Array<String>) = executeCommand(CommandArguments(args))
    final override fun addTabCompletionOptions(sender: ICommandSender?, args: Array<out String>?, pos: BlockPos?): MutableList<String> = getListOfStringsMatchingLastWord(args, shortcuts)

    abstract fun executeCommand(args: CommandArguments)
    abstract val shortcuts: List<String>
}