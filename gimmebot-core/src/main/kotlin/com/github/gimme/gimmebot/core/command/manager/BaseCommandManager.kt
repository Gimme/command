package com.github.gimme.gimmebot.core.command.manager

import com.github.gimme.gimmebot.core.command.CommandSender
import com.github.gimme.gimmebot.core.command.Command
import kotlin.collections.Collection
import kotlin.collections.HashMap
import kotlin.collections.drop
import kotlin.collections.set


/**
 * Represents a command manager with base functionality.
 */
abstract class BaseCommandManager(
    /** Prefix required to execute commands. */
    private var commandPrefix: String,
) : CommandManager {

    private val commandByName = HashMap<String, Command>()

    override fun registerCommand(command: Command) {
        commandByName[command.name] = command
    }

    override fun getCommands(): Collection<Command> {
        return commandByName.values
    }

    override fun getCommand(name: String): Command? {
        return commandByName[name]
    }

    override fun parseInput(commandSender: CommandSender, input: String): Boolean {
        var lowerCaseInput = input.toLowerCase()

        if (commandSender.medium.requiresCommandPrefix) {
            // Has to start with the command prefix
            if (!lowerCaseInput.startsWith(commandPrefix)) return false
            // Remove prefix
            lowerCaseInput = lowerCaseInput.substring(commandPrefix.length)
        }

        // Split into words on spaces, ignoring spaces between two quotation marks
        val words = lowerCaseInput.split("\\s(?=(?:[^\\\"]*\\\"[^\\\"]*\\\")*[^\\\"]*\$)".toRegex())
            .map { s -> s.replace("\"", "") }

        val commandName = words[0]
        val args = words.drop(1)

        // Return if not a valid command
        val command = getCommand(commandName) ?: return false

        // Execute the command
        command.execute(commandSender, args)?.sendTo(commandSender)
        return true
    }
}
