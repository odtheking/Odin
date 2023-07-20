package me.odinclient.commands

import me.odinclient.utils.skyblock.ChatUtils.modMessage
import net.minecraft.command.CommandBase
import net.minecraft.command.ICommandSender
import net.minecraft.util.BlockPos

abstract class AbstractCommand(
    val name: String,
    private val alias: ArrayList<String> = arrayListOf(),
    val description: String = ""
) : CommandBase() {

    final override fun getCommandName(): String {
        return name
    }

    final override fun getCommandAliases(): MutableList<String> {
        return alias
    }

    final override fun getRequiredPermissionLevel(): Int {
        return 0
    }

    final override fun getCommandUsage(sender: ICommandSender): String {
        return "/$name"
    }

    final override fun addTabCompletionOptions(
        sender: ICommandSender?,
        args: Array<out String>,
        pos: BlockPos?
    ): MutableList<String> {
        return if (args.size == 1) getListOfStringsMatchingLastWord(args, tabCompList)
        else mutableListOf()
    }

    final override fun processCommand(sender: ICommandSender?, args: Array<out String>) {
        try {
            val commandName = args.getOrNull(0) ?: ""
            val function = subcommands[commandName] ?: subcommands[null] ?: return modMessage(errorMsg)
            function(CommandArguments(args))
        } catch (e: Throwable) {
            modMessage(e.message!!)
        }
    }

    val subcommands = HashMap<String?, (CommandArguments) -> Unit>()

    var commandDescription = "Help for this command."
    val tabCompList = ArrayList<String>()

    open val errorMsg: String = "Sub-command not recognized."

    infix fun String?.does(action: (CommandArguments) -> Unit): SubCommand {
        subcommands[this] = action
        if (!this.isNullOrBlank()) tabCompList.add(this)
        return SubCommand(this, action)
    }

    infix fun SubCommand.description(string: String): SubCommand {
        if (!name.isNullOrBlank()) commandDescription += string
        return this
    }

    /**
     * Acts as a pair to simplify code.
     */
    class SubCommand(val name: String?, inline val action: (CommandArguments) -> Unit)
}