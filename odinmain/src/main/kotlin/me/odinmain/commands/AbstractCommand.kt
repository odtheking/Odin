package me.odinmain.commands

import me.odinmain.utils.skyblock.modMessage
import net.minecraft.command.CommandBase
import net.minecraft.command.ICommandSender
import net.minecraft.util.BlockPos

/**
 * ## Abstract Command
 *
 * This class allows to create commands with an improved experience
 * while keeping almost all the flexibility and performance.
 *
 * For example:
 * ```
 *  object Command : AbstractCommand("commandName", "cmdName") {
 *      init {
 *          "hello" {
 *              sendError("Invalid use. Correct Use: world, hey")
 *              "world" does {
 *                  println("hello")
 *              }
 *              "hey" {
 *                  // and so on...
 *              }
 *          }
 *      }
 *  }
 * ```
 *
 * Using [subcommands][Subcommand] allows for advanced auto tab competitions (without having to write any of the code!)
 *
 * @author Stivais
 * @see [Subcommand]
 */
abstract class AbstractCommand(
    private vararg val names: String,
) : CommandBase() {

    final override fun getCommandName() = names[0]
    final override fun getCommandAliases() = names.drop(1)
    final override fun getRequiredPermissionLevel() = 0
    final override fun getCommandUsage(sender: ICommandSender) = "/$commandName"

    /**
     * Loops through [subcommands] to find a match.
     *
     * If args are empty and [BaseFunction] is present it will run that
     *
     * If it's unable to find anything in [subcommands] and [extraCmd] is present it will run that.
     */
    final override fun processCommand(sender: ICommandSender?, args: Array<String>) {
        for (i in subcommands.size - 1 downTo 0) { // doing this is soo much simpler than like tree system i cba
            if (subcommands[i].argsRequired.all { it in args }) {
                subcommands[i].execute(args)
                return
            }
        }

        baseFunction?.let {
            it(args)
        }
    }

    /**
     * Loops through [subcommands] to find a match.
     *
     * Will only provide valid [Subcommands][Subcommand]. So ones that it doesn't provide ones that shouldn't be shown
     */
    final override fun addTabCompletionOptions(
        sender: ICommandSender?, args: Array<out String>, pos: BlockPos?,
    ): List<String> {
        if (args.size == 1) {
            return subcommands
                .filter { it.parent == null && it.name.startsWith(args[0], true) }
                .map { it.name }
        }

        for (i in subcommands.size - 1 downTo 0) {
            if (subcommands[i].argsRequired.all { it in args }) {
                return subcommands[i].children
                    .map { it.name }
                    .filter { it.startsWith(args.last()) }
            }
        }
        return listOf()
    }

    /**
     * List containing all the subcommands.
     */
    val subcommands = ArrayList<Subcommand>()

    /**
     * Function for base command, aka if args are empty or gibberish.
     */
    private var baseFunction: ((Array<out String>) -> Unit)? = null

    /**
     * Sets the function of the base command.
     */
    fun does(block: (Array<out String>) -> Unit) {
        baseFunction = block
    }

    /**
     * DSL
     */
    operator fun String.invoke(block: Subcommand.() -> Unit): Subcommand {
        return Subcommand(this, this@AbstractCommand).apply {
            subcommands.add(this)
            block()
        }
    }

    /**
     * DSL, looks nicer, etc.
     */
    fun Subcommand.does(func: (Array<out String>) -> Unit) {
        this.func = func
    }

    /**
     * Use to simplify sending error messages if arguments are not met.
     *
     * Is literally just:
     * does {
     *     modMessage(msg)
     * }
     */
    fun Subcommand.sendError(message: String) {
        this.does { modMessage(message) }
    }

    fun sendError(message: String) {
        this.does { modMessage(message) }
    }

    /**
     * Creates and initializes the function for the sub-command
     */
    infix fun String.does(func: (Array<out String>) -> Unit): Subcommand {
        return this {
            does(func)
        }
    }
}

/**
 * Factory so instead of creating a whole class to make init block it just thing
 */
operator fun String.invoke(block: AbstractCommand.() -> Unit): AbstractCommand {
    return object : AbstractCommand(this) {
        init {
            block()
        }
    }
}