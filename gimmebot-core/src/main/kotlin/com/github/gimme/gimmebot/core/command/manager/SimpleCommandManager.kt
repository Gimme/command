package com.github.gimme.gimmebot.core.command.manager

import com.github.gimme.gimmebot.core.command.CommandSender
import com.github.gimme.gimmebot.core.command.Command
import com.github.gimme.gimmebot.core.command.HelpCommand
import com.github.gimme.gimmebot.core.command.manager.commandcollection.CommandCollection
import com.github.gimme.gimmebot.core.command.manager.commandcollection.CommandTree
import kotlin.collections.drop


/**
 * Represents a command manager with base functionality.
 *
 * @param commandPrefix prefix required to execute commands from chat
 */
class SimpleCommandManager(
    private var commandPrefix: String = "/",
) : CommandManager {

    private val commandCollection: CommandCollection = CommandTree()

    init {
        registerCommand(HelpCommand(commandCollection))
    }

    override fun registerCommand(command: Command) {
        commandCollection.addCommand(command)
    }

    override fun getCommand(name: String): Command? {
        return commandCollection.getCommand(name)
    }

    override fun getCommandCollection(): CommandCollection {
        return commandCollection
    }

    override fun parseInput(commandSender: CommandSender, input: String): Boolean {
        var lowerCaseInput = input.toLowerCase()

        if (commandSender.medium.requiresCommandPrefix) {
            // Has to start with the command prefix
            if (!lowerCaseInput.startsWith(commandPrefix)) return false
            // Remove prefix
            lowerCaseInput = lowerCaseInput.substring(commandPrefix.length)
        }

        // Return if not a valid command
        val command = getCommand(lowerCaseInput) ?: return false
        // Remove command name, leaving only the arguments
        lowerCaseInput = lowerCaseInput.removePrefix(command.name)

        // Split into words on spaces, ignoring spaces between two quotation marks
        val args = lowerCaseInput.split("\\s(?=(?:[^\"]*\"[^\"]*\")*[^\"]*\$)".toRegex())
            .map { s -> s.replace("\"", "") }.drop(1)

        // Execute the command
        command.execute(commandSender, args)?.sendTo(commandSender)
        return true
    }
}
