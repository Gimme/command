package dev.gimme.command.commands

import dev.gimme.command.Command
import dev.gimme.command.function.FunctionCommand
import dev.gimme.command.function.CommandFunction

/**
 * Displays a list of available commands.
 */
class HelpCommand(private val commands: () -> Iterable<Command<*>>) : FunctionCommand<List<HelpCommand.CommandHelp>>("help") {

    /** Prints available commands. */
    @CommandFunction
    fun printCommands(): List<CommandHelp> = commands().map { CommandHelp(it.path(" "), it.usage) }

    /**
     * Help info about a command.
     *
     * @property name the name of the command
     * @property usage the usage info of the command
     */
    data class CommandHelp(
        val name: String,
        val usage: String,
    )
}
