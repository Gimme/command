package dev.gimme.gimmeapi.command.channel

import dev.gimme.gimmeapi.command.sender.CommandSender

/**
 * Can send responses back to the command sender.
 */
interface CommandOutputSender<in R> {

    /** Sends the given [response] to the given command [commandSender]. */
    fun respond(commandSender: CommandSender, response: R)
}
