package com.github.gimme.gimmebot.core.command

import com.github.gimme.gimmebot.core.command.manager.CommandManager
import com.github.gimme.gimmebot.core.command.executor.CommandExecutor
import com.github.gimme.gimmebot.core.command.manager.commandcollection.CommandCollection

/**
 * Displays a list of available commands.
 */
class HelpCommand(
    private val commandCollection: CommandCollection,
) : BaseCommand("help") {

    /** Prints available commands. */
    @CommandExecutor
    fun printCommands(): CommandResponse {
        val sb = StringBuilder("Commands:")

        commandCollection.getCommands().forEach { command ->
            sb.append("\n")
            sb.append("  ")
            sb.append(command.getUsage())
        }

        return CommandResponse(sb.toString())
    }
}
