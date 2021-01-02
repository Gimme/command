package com.github.gimme.gimmebot.core.command.medium

import com.github.gimme.gimmebot.core.command.MessageReceiver
import com.github.gimme.gimmebot.core.command.manager.CommandManager
import com.github.gimme.gimmebot.core.common.Enableable

/**
 * Represents a medium through which commands can be sent and responses received.
 *
 * @param R the output response type
 */
interface CommandMedium<R> : CommandInputHandler, CommandOutputHandler<R>, Enableable {

    /** This medium's main command manager. */
    val commandManager: CommandManager<R>

    /** This medium's command managers. */
    val commandManagers: List<CommandManager<*>>

    /**
     * Registers the given [commandManager] making the contained commands executable through this medium with the
     * specified [responseWrapper] to convert the results from [T] to the compatible type [R].
     */
    fun <T> registerCommandManager(commandManager: CommandManager<T>, responseWrapper: (T) -> R)

    /** Adds the given [messageReceiver] to be sent all command input and output. */
    fun addIOListener(messageReceiver: MessageReceiver)
}
