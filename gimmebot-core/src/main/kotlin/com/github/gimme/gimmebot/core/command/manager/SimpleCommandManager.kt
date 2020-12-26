package com.github.gimme.gimmebot.core.command.manager

import com.github.gimme.gimmebot.core.command.Command
import com.github.gimme.gimmebot.core.command.CommandSender
import com.github.gimme.gimmebot.core.command.HelpCommand
import com.github.gimme.gimmebot.core.command.manager.commandcollection.CommandTree


/**
 * Represents a command manager with base functionality.
 */
class SimpleCommandManager : CommandManager {

    override val commandCollection: CommandTree<Any?> = CommandTree()

    init {
        registerCommand(HelpCommand(commandCollection))
    }

    override fun registerCommand(command: Command<*>) {
        commandCollection.addCommand(command)
    }

    override fun getCommand(name: String): Command<*>? {
        return commandCollection.getCommand(name)?.command
    }

    override fun executeCommand(commandSender: CommandSender, commandName: String, arguments: List<String>): Any? {
        // Return if not a valid command
        val command = getCommand(commandName.toLowerCase()) ?: return false // TODO: command error: not a command

        return command.execute(commandSender, arguments)
    }
}
