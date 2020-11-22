package com.github.gimme.gimmebot.core.command

/**
 * Represents a command manager with base functionality.
 */
abstract class BaseCommandManager : CommandManager {

    private val commandByName = HashMap<String, Command>()

    override fun registerCommand(command: Command) {
        commandByName[command.name] = command
    }

    override fun getCommand(name: String): Command? {
        return commandByName[name]
    }

    override fun executeCommand(commandSender: CommandSender, command: Command) {
        val response: CommandResponse? = command.execute(commandSender)

        response?.send(commandSender)
    }
}
