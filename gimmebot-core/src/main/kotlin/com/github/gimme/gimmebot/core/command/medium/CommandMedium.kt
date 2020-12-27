package com.github.gimme.gimmebot.core.command.medium

import com.github.gimme.gimmebot.core.command.MessageReceiver
import com.github.gimme.gimmebot.core.command.manager.CommandManager

/**
 * Represents a medium through which commands can be sent and responses received.
 *
 * @param R the response type
 */
interface CommandMedium<R> : CommandInputHandler, CommandOutputHandler<R> {

    /** The command manager that handles this medium's available commands. */
    var commandManager: CommandManager<R>

    /** Installs this command input medium. */
    fun install()

    /** Adds the given [messageReceiver] to be sent all command input and output. */
    fun addIOListener(messageReceiver: MessageReceiver)
}
