package dev.gimme.gimmeapi.core.command.channel

import dev.gimme.gimmeapi.core.command.manager.CommandManager
import dev.gimme.gimmeapi.core.command.sender.MessageReceiver
import dev.gimme.gimmeapi.core.common.Enableable

/**
 * Represents a channel through which commands can be sent and responses received.
 *
 * @param R the output response type
 */
interface CommandChannel<R> : CommandInputParser, CommandOutputSender<R>, Enableable, CommandManager.RegisterCommandListener {

    /** This channel's main command manager. */
    val commandManager: CommandManager<R>

    /** This channel's command managers. */
    val commandManagers: List<CommandManager<*>>

    /**
     * Registers the given [commandManager] making the contained commands executable through this channel with the
     * specified [responseWrapper] to convert the results from [T] to the compatible type [R].
     */
    fun <T> registerCommandManager(commandManager: CommandManager<T>, responseWrapper: (T) -> R)

    /** Adds the given [messageReceiver] to be sent all command input and output. */
    fun addIOListener(messageReceiver: MessageReceiver)
}
