package me.odinclient.commands

/**
 * ## Subcommand
 *
 * This class provides a flexible and extensible system for creating hierarchies of commands,
 * enabling the nesting of subcommands within each other, resulting in a more sophisticated command structure.
 *
 * A Subcommand object can have a parent, which establishes a parent-child relationship among commands.
 * This makes it convenient to create command trees.
 */
class Subcommand(val name: String, private val root: AbstractCommand, var parent: Subcommand? = null) {

    var func: ((Array<out String>) -> Unit)? = null

    operator fun String.invoke(block: Subcommand.() -> Unit): Subcommand {
        return Subcommand(this, root).apply {
            root.subcommands.add(this)

            parent = this@Subcommand
            argsRequired += name
            this@Subcommand.children.add(this)
            block()
        }
    }

    infix fun String.does(block: (Array<out String>) -> Unit): Subcommand = this { func = block }

    /**
     * Children of this class.
     */
    val children = ArrayList<Subcommand>()

    /**
     * Args required to execute or auto complete.
     * (All the parent's names + it's name)
     *
     * @see AbstractCommand.processCommand
     * @see AbstractCommand.addTabCompletionOptions
     */
    var argsRequired = arrayOf(name)

    /**
     * Invokes this classes function (if it's present)
     */
    fun execute(args: Array<out String>) {
        func?.let {
            it(args.copyOfRange(argsRequired.size, args.size))
        }
    }
}