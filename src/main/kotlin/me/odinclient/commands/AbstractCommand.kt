package me.odinclient.commands

import me.odinclient.commands.impl.AutoSellCommand.cmds
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
        execute(args)
        println(System.nanoTime() - e)
    }

    internal val cmds = ArrayList<Subcommand>()

    private var emptyCmd: ((Array<out String>) -> Unit)? = null
    private var extraCmd: ((Array<out String>) -> Unit)? = null

    fun empty(func: (Array<out String>) -> Unit) {
        emptyCmd = func
    }

    // TODO: Rename
    fun orElse(func: (Array<out String>) -> Unit) {
        extraCmd = func
    }

    infix fun String.cmd(block: Subcommand.() -> Unit): Subcommand {
        return Subcommand(this).apply {
            block()
        }
    }

    operator fun String.minus(block: Subcommand.() -> Unit): Subcommand {
        return this.cmd(block)
    }

    fun Subcommand.does(func: (Array<out String>) -> Unit) {
        this.func = func
    }

    infix fun String.does(func: (Array<out String>) -> Unit): Subcommand {
        return Subcommand(this, func)
    }

    fun Subcommand.and(vararg cmd: Subcommand) {
        children.addAll(cmd)
        initChildren()
    }

    // TODO: Make it cut off the part of array used for finding subcommand
    // TODO: For example, args are "add temp x y z" but it will dispatch only x y z to subcommand temp under add
    fun execute(args: Array<out String>) {
        if (args.isEmpty()) {
            emptyCmd?.let {
                it(args)
                return
            }
        }

        val string = args.joinToString(" ") { it.lowercase() }
        for (i in cmds) {
            if (string == i.lineageName) {
                i.func?.let { it(args) }
                return
            }
        }
        extraCmd?.let { it(args) }
    }

    class Subcommand(val name: String, inline var func: ((Array<out String>) -> Unit)? = null) {
        val children = ArrayList<Subcommand>()

        var parent: Subcommand? = null
        var lineageName = name

        init {
            cmds.add(this)
        }

        // shouldn't really matter that it's being run a few more times than needed since its only done once.
        fun initChildren() {
            for (i in children) {
                if (i.parent == null) i.parent = this
                i.lineageName = "$lineageName ${i.name}"
                i.initChildren()
            }
        }
    }
}