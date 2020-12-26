package com.github.gimme.gimmebot.core.command.medium

import com.github.gimme.gimmebot.core.command.MessageReceiver
import com.github.gimme.gimmebot.core.command.manager.commandcollection.CommandCollection

/**
 * Represents a medium through which commands can be sent.
 *
 * @param R the response type
 */
interface CommandMedium<R> {

    /** The collection of commands that this medium handles. */
    var commandCollection: CommandCollection<R>

    /** Installs this command input medium. */
    fun install()

    /** Adds the given [messageReceiver] to be sent all command input and output. */
    fun addIOListener(messageReceiver: MessageReceiver)
}
