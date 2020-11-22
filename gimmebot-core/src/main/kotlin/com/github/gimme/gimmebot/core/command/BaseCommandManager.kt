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
}
