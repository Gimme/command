package com.github.gimme.gimmebot.core.command.manager

import com.github.gimme.gimmebot.core.command.Command
import com.github.gimme.gimmebot.core.command.CommandSender
import com.github.gimme.gimmebot.core.command.MessageReceiver
import com.github.gimme.gimmebot.core.command.manager.commandcollection.CommandTree
import com.github.gimme.gimmebot.core.command.medium.CommandInputMedium

/**
 * Represents a command manager that handles the registration and execution of commands.
 */
interface CommandManager {

    /** The mutable collection of all registered commands. */
    val commandCollection: CommandTree

    /** Registers the given [command] to be executable by this command manager. */
    fun registerCommand(command: Command<*>)

    /** Returns the command with the specified [name] if it has been registered. */
    fun getCommand(name: String): Command<*>?

    /** Adds the given [messageReceiver] to be sent all command output. */
    fun addOutputListener(messageReceiver: MessageReceiver)

    /**
     * Checks the given [commandName] and [arguments] if a valid command call and then executes it as the given
     * [commandSender]. Returns if the command was successfully executed.
     */
    fun executeCommand(commandSender: CommandSender, commandName: String, arguments: List<String> = listOf()): Boolean

    /** Installs the given [commandInputMedium] to accept command input for this manager. */
    fun install(commandInputMedium: CommandInputMedium) {
        commandInputMedium.install(this)
    }
}
