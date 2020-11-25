package com.github.gimme.gimmebot.core.command

/**
 * Represents a command manager with base functionality.
 */
abstract class BaseCommandManager(
    /** Prefix required to execute commands. */
    var commandPrefix: String,
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
        var lowerCaseInput = input.toLowerCase().trimEnd()

        if (commandSender.medium.requiresCommandPrefix) {
            // Has to start with the command prefix
            if (!lowerCaseInput.startsWith(commandPrefix)) return false
            // Remove prefix
            lowerCaseInput = lowerCaseInput.substring(commandPrefix.length)
        }

        // Split into words
        val words = lowerCaseInput.split(" ")
        val commandName = words[0]
        val args = words.drop(1)

        // Return if not a valid command
        val command = getCommand(commandName) ?: return false

        val response: CommandResponse? = command.execute(commandSender, args)
        response?.sendTo(commandSender)
        return true
    }
}
