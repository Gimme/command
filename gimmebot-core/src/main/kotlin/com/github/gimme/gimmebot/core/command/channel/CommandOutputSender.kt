package com.github.gimme.gimmebot.core.command.channel

import com.github.gimme.gimmebot.core.command.sender.CommandSender

/**
 * Can send responses back to the command sender.
 */
interface CommandOutputSender<in R> {

    /** Sends the given [response] to the given command [commandSender]. */
    fun respond(commandSender: CommandSender, response: R)
}
