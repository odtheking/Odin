package me.odinclient.commands

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.odinclient.OdinClient.Companion.scope
import me.odinclient.commands.impl.AutoSellCommand.map
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

    // TODO: Add a system of subclasses under subclasses somehow so this will auto complete only stuff under a certain subcommand
    final override fun addTabCompletionOptions(
        sender: ICommandSender?,
        args: Array<out String>,
        pos: BlockPos?
    ): MutableList<String> {
        return mutableListOf()
    }

    final override fun processCommand(sender: ICommandSender?, args: Array<String>) {
        val e = System.nanoTime()
        val subcmd =
            map[args.getOrNull(0) ?: ""] ?: map[null] ?: return modMessage("Sub-command not recognized.")
        subcmd.execute(args)
        println(System.nanoTime() - e)
    }

    internal val map = HashMap<String?, Subcommand>()

    private infix fun <E> String.subcommand(block: Subcommand.() -> E): Subcommand {
        return Subcommand(this).apply {
            block().let {
                if (it is Subcommand) {
                    subcommands.add(it)
                    it.parent = this
                }
            }
        }
    }

    infix fun String.does(block: Action): Subcommand {
        return Subcommand(this, block)
    }

    fun Subcommand.does(block: Action) {
        this.action = block
    }

    fun empty(action: Action?) {
        map[""] = Subcommand("", action)
    }

    /**
     *  This looks nicer than using [subcommand]
     *
     *  Code goes from:
     *  ```
     *  "subcommand" subcmd {
     *      // do stuff..
     *  }
     *  ```
     *  to
     *  ```
     *  "subcommand" - {
     *      // do stuff..
     *  }
     *  ```
     */
    operator fun <E> String.minus(block: Subcommand.() -> E): Subcommand {
        return this.subcommand(block)
    }

    operator fun String.minusAssign(block: Action) {
        this.does(block)
    }

    // It may be better to use subcommand as a simple wrapper for data and then just put it together with the action in a hashmap
    class Subcommand(val name: String? = null, var action: Action? = null, vararg subcommands: Subcommand) {

        val subcommands = ArrayList<Subcommand>().apply { addAll(subcommands) }
        var parent: Subcommand? = null

        init {
            this.subcommands.forEach {
                it.parent = this
            }

            scope.launch {
                delay(100)
                if (parent == null) map[name] = this@Subcommand
            }
        }

        // TODO: IMPROVE PERFORMANCE
        fun findSubcommand(args: Array<out String>, index: Int = 0): Subcommand? {
            if (name.isNullOrBlank()) return this
            println("$name, ${args[index]}")
            if (args[index] != name) return null
            if (args.size == 1 + index) return this

            for (sub in subcommands) {
                sub.findSubcommand(args, index + 1)?.let {
                    return it
                }
            }
            return null
        }

        // TODO: IMPROVE PERFORMANCE
        fun execute(args: Array<String>) {
            val lowercaseArgs = args.onEach { it.lowercase() }

            findSubcommand(lowercaseArgs)?.let {
                it.action?.let { it(lowercaseArgs) }
            }
        }
    }
}

typealias Action = (Array<out String>) -> Unit