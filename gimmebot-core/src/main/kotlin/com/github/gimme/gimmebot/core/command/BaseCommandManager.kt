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

    override fun getCommand(name: String): Command? {
        return commandByName[name]
    }

    override fun parseInput(commandSender: CommandSender, input: String) {
        val lowerCaseInput = input.toLowerCase().trimEnd()
        if (!lowerCaseInput.startsWith(commandPrefix)) return

        val words = lowerCaseInput.substring(commandPrefix.length).split(" ")
        val command = getCommand(words[0])

        val response: CommandResponse? = command?.execute(commandSender, words.drop(1))
        response?.sendTo(commandSender)
    }
}
