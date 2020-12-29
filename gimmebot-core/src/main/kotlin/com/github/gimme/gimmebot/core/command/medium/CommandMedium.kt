package com.github.gimme.gimmebot.core.command.medium

import com.github.gimme.gimmebot.core.command.MessageReceiver
import com.github.gimme.gimmebot.core.command.manager.CommandManager

/**
 * Represents a medium through which commands can be sent and responses received.
 *
 * @param R the output response type
 */
interface CommandMedium<in R> : CommandInputHandler, CommandOutputHandler<R> {

    /** Installs this command input medium. */
    fun install()

    /**
     * Registers the given [commandManager] making the contained commands executable through this medium with the
     * specified [responseWrapper] to convert the results from [T] to the compatible type [R].
     */
    fun <T> registerCommandManager(commandManager: CommandManager<T>, responseWrapper: (T) -> R)

    /** Adds the given [messageReceiver] to be sent all command input and output. */
    fun addIOListener(messageReceiver: MessageReceiver)
}
