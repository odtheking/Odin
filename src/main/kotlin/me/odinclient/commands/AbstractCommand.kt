package me.odinclient.commands

import me.odinclient.commands.AbstractCommand.Subcommand
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
 *  object Command : AbstractCommand("commandName", "cmdName", description = "Description...") {
 *      init {
 *          "hello" - {
 *              does { println("hello") }
 *              and (
 *                  "world" does {
 *                      println("hello")
 *                  },
 *                  "hey" - {
 *                      // and so on...
 *                  }
 *              )
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
    val description: String = ""
) : CommandBase() {

    final override fun getCommandName() = names[0]
    final override fun getCommandAliases() = names.drop(1)
    final override fun getRequiredPermissionLevel() = 0
    final override fun getCommandUsage(sender: ICommandSender) = "/$commandName"

    /**
     * Loops through [subcommands] to find a match.
     *
     * If args are empty and [emptyCmd] is present it will run that
     *
     * If it's unable to find anything in [subcommands] and [extraCmd] is present it will run that.
     */
    final override fun processCommand(sender: ICommandSender?, args: Array<String>) {
        if (args.isEmpty()) {
            emptyCmd?.let {
                it(args)
                return
            }
        }

        for (i in subcommands.size - 1 downTo 0) {
            if (subcommands[i].argsRequired.all { it in args }) {
                subcommands[i].execute(args)
                return
            }
        }
        extraCmd?.let { it(args) }
    }

    /**
     * Loops through [subcommands] to find a match.
     *
     * Will only provide valid [Subcommands][Subcommand]. So ones that it doesn't provide ones that shouldn't be shown
     */
    final override fun addTabCompletionOptions(
        sender: ICommandSender?, args: Array<out String>, pos: BlockPos?
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
    private val subcommands = ArrayList<Subcommand>()

    /**
     * A function that gets run when arguments are empty.
     */
    private var emptyCmd: ((Array<out String>) -> Unit)? = null

    /**
     * A function that gets run when arguments can be varying.
     *
     * Can also be used to provide error message rather than what would usually be nothing.
     */
    private var extraCmd: ((Array<out String>) -> Unit)? = null

    // TODO: Add a description system and automatic help command.

    /**
     * ## Subcommand
     *
     * This class provides a flexible and extensible system for creating hierarchies of commands,
     * enabling the nesting of subcommands within each other, resulting in a more sophisticated command structure.
     *
     * A Subcommand object can have a parent, which establishes a parent-child relationship among commands.
     * This makes it convenient to create comprehensive command trees.
     */
    class Subcommand(val name: String, inline var func: ((Array<out String>) -> Unit)? = null) {
        /**
         * Children of this class.
         */
        val children = ArrayList<Subcommand>()

        /**
         * Parent of this class.
         */
        var parent: Subcommand? = null

        /**
         * Args required to execute or auto complete.
         * (All the parent's names + it's name)
         *
         * @see processCommand
         * @see addTabCompletionOptions
         */
        var argsRequired = arrayOf(name)

        /**
         * Invokes this classes function (if it's present.)
         */
        fun execute(args: Array<out String>) {
            func?.let {
                it(args.copyOfRange(argsRequired.size, args.size))
            }
        }

        /**
         * Initializes the [children] of this class.
         * also sets the [parent] and [argsRequired]
         *
         * It runs a few more times than needed if nested due to everything being initialized backwards
         */
        fun initChildren() {
            for (i in children) {
                if (i.parent == null) i.parent = this

                i.argsRequired = argsRequired.plus(i.name)
                i.initChildren()
            }
        }
    }

    /**
     * Sets the [emptyCmd] function.
     */
    fun empty(func: (Array<out String>) -> Unit) {
        emptyCmd = func
    }

    // TODO: Rename
    /**
     * Sets the [extraCmd] function.
     */
    fun orElse(func: (Array<out String>) -> Unit) {
        extraCmd = func
    }

    /**
     * Provides a cleaner way to create subcommands.
     * ```
     *  // Code goes from:
     *
     *  Subcommand(
     *      name = "hello",
     *      action = null,
     *      Subcommand(
     *          name = "world",
     *          action = { println("Hello World") }
     *      ), // and so on
     *  )
     *
     *  // To this:
     *
     *  "hello" cmd {
     *      and(
     *          "world" cmd {
     *              does { println("Hello World") }
     *              and(
     *                  // and so on
     *              )
     *          }
     *      )
     *  }
     * ```
     * @see minus
     * @see does
     * @see and
     */
    infix fun String.cmd(block: Subcommand.() -> Unit): Subcommand {
        return Subcommand(this).apply {
            subcommands.add(this)
            block()
        }
    }

    /**
     * Acts as [cmd].
     *
     * This makes the code cleaner nice by replacing the "cmd" with a "-"
     * ```
     *  // For example:
     *  "hello" cmd {
     *      and(
     *          "world" cmd {
     *          }
     *      }
     *  }
     *
     *  // Turns into
     *  "hello" {
     *      and(
     *          "world" {
     *          }
     *      }
     *  }
     * ```
     * @see cmd
     * @see and
     * @see does
     */
    operator fun String.invoke(block: Subcommand.() -> Unit): Subcommand {
        return this.cmd(block)
    }

    /**
     * Initializes the function for the sub-command
     */
    fun Subcommand.does(func: (Array<out String>) -> Unit) {
        this.func = func
    }

    /**
     * Creates and initializes the function for the sub-command
     */
    infix fun String.does(func: (Array<out String>) -> Unit): Subcommand {
        return this.cmd { does(func) }
    }

    /**
     * Allows to initialize children under a subcommand.
     */
    fun Subcommand.and(vararg cmd: Subcommand) {
        children.addAll(cmd)
        initChildren()
    }
}