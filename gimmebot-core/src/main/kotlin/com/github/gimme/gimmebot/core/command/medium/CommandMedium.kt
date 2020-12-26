package com.github.gimme.gimmebot.core.command.medium

import com.github.gimme.gimmebot.core.command.MessageReceiver
import com.github.gimme.gimmebot.core.command.manager.commandcollection.CommandCollection

/**
 * Represents a medium through which commands can be sent.
 *
 * For example a chat box or a command line interface.
 */
interface CommandMedium {

    /** The collection of commands that this medium handles. */
    var commandCollection: CommandCollection

    /** Installs this command input medium. */
    fun install()

    /** Adds the given [messageReceiver] to be sent all command input and output. */
    fun addIOListener(messageReceiver: MessageReceiver)
}
