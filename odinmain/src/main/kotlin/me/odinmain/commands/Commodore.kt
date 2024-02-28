@file:Suppress("UNUSED", "NOTHING_TO_INLINE")

package me.odinmain.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.exceptions.CommandSyntaxException
import me.odinmain.utils.skyblock.modMessage
import net.minecraft.command.CommandBase
import net.minecraft.command.ICommandSender
import net.minecraft.util.BlockPos
import net.minecraftforge.client.ClientCommandHandler

/**
 * Legacy implementation for [Commodore](https://github.com/Stivais/Commodore)
 * A library that uses kotlin DSL to provide a cleaner alternative to brigadier for commands.
 *
 * @author Stivais
 */
fun commodore(vararg string: String, block: com.github.stivais.commodore.CommandNode<Int>.() -> Unit): CommandBase {
    // temporary aliases fix
    return object : CommandBase() {

        private val root = com.github.stivais.commodore.CommandNode<Int>(string[0])

        private val dispatcher = CommandDispatcher<Int>()

        init {
            root.block()
            root.setup()
            dispatcher.register(root.builder)
        }

        override fun getCommandName(): String = string[0]

        override fun getCommandAliases(): List<String> = string.drop(1)

        override fun getCommandUsage(sender: ICommandSender?): String = "/${commandName}"

        override fun getRequiredPermissionLevel(): Int = 0

        override fun processCommand(sender: ICommandSender, args: Array<out String>) {
            try {
                dispatcher.execute(args.fix(), 1)
            } catch (e: CommandSyntaxException) {
                modMessage("Invalid usage, look at /help") // some1 else change this to be better
            }
        }

        override fun addTabCompletionOptions(
            sender: ICommandSender,
            args: Array<out String>,
            pos: BlockPos,
        ): List<String> {
            val result = dispatcher.parse(args.fix(), 1)
            return dispatcher.getCompletionSuggestions(result).get().list.map { it.text }
        }

        inline fun Array<out String>.fix(): String {
            return if (this.isEmpty()) commandName else "$commandName ${this.joinToString(separator = " ")}"
        }
    }
}

/**
 * Registers an array of commands
 *
 * @param command The array of [commands][CommandBase]
 */
fun registerCommands(vararg command: CommandBase) {
    for (i in command) {
        ClientCommandHandler.instance.registerCommand(i)
    }
}



@Deprecated("don't use, im just to lazy to convert atm")
interface Commodore : ICommodore {
    override val command: CommandNode

    companion object {
        fun registerCommands(vararg commands: Commodore) {
            for (command in commands) {
                command.registerCommands()
            }
        }

        fun Commodore.registerCommands() {
            val cmd = command
            ClientCommandHandler.instance.registerCommand(
                object : CommandBase() {
                    val dispatcher = CommandDispatcher<Any>()

                    init {
                        cmd.setup()
                        dispatcher.register(cmd.builder)
                    }

                    override fun getCommandName(): String = cmd.name

                    override fun getCommandUsage(sender: ICommandSender?): String = "/${commandName}"

                    override fun getRequiredPermissionLevel(): Int = 0

                    override fun processCommand(sender: ICommandSender, args: Array<out String>) {
                        val time = System.currentTimeMillis()
                        try {
                            dispatcher.execute(args.fix(), null)
                        } catch (e: CommandSyntaxException) {
                            modMessage(e.message ?: return)
                        }
                        println(System.currentTimeMillis() - time)
                    }

                    override fun addTabCompletionOptions(
                        sender: ICommandSender,
                        args: Array<out String>,
                        pos: BlockPos,
                    ): List<String> {
                        val result = dispatcher.parse(args.fix(), null)
                        return dispatcher.getCompletionSuggestions(result).get().list.map { it.text }
                    }

                    inline fun Array<out String>.fix(): String {
                        return if (this.isEmpty()) commandName else "$commandName ${this.joinToString(separator = " ")}"
                    }
                }
            )
        }
    }
}

typealias ICommodore = com.github.stivais.commodore.Commodore<Any>
typealias CommandNode = com.github.stivais.commodore.CommandNode<Any>
