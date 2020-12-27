package com.github.gimme.gimmebot.core.command.medium

import com.github.gimme.gimmebot.core.command.CommandSender
import com.github.gimme.gimmebot.core.command.MessageReceiver
import com.github.gimme.gimmebot.core.command.manager.CommandManager

/**
 * Represents a command input medium with base functionality.
 *
 * @param R the response type
 */
abstract class BaseCommandMedium<R>(override var commandManager: CommandManager<R>) : CommandMedium<R> {

    private val ioListeners: MutableList<MessageReceiver> = mutableListOf()

    init {
        addIOListener { ConsoleCommandSender }
    }

    override fun parseInput(sender: CommandSender, input: String) {
        ioListeners.forEach { it.sendMessage("${sender.name}: $input") }
    }

    override fun respond(commandSender: CommandSender, response: R) {
        ioListeners.forEach { it.sendMessage(response.toString()) }
    }

    final override fun addIOListener(messageReceiver: MessageReceiver) {
        ioListeners.add(messageReceiver)
    }

    override fun install() {
        onInstall()
    }

    /** Performs logic when installed. */
    protected abstract fun onInstall()
}
