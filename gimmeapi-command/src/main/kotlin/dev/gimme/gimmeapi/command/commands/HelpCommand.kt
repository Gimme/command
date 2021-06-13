package dev.gimme.gimmeapi.command.commands

import dev.gimme.gimmeapi.command.function.FunctionCommand
import dev.gimme.gimmeapi.command.function.CommandExecutor
import dev.gimme.gimmeapi.command.channel.CommandChannel

/**
 * Displays a list of available commands.
 */
class HelpCommand(private val commandChannel: CommandChannel<*>) : FunctionCommand<List<HelpCommand.CommandHelp>>("help") {

    /** Prints available commands. */
    @CommandExecutor
    fun printCommands(): List<CommandHelp> {

        val list: MutableList<CommandHelp> = mutableListOf()

        commandChannel.commandManagers.forEach { commandManager ->
            commandManager.commands.forEach {
                list.add(CommandHelp(it.path(" "), it.usage))
            }
        }

        return list
    }

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
