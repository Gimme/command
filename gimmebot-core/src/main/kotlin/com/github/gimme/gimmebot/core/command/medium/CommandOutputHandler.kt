package com.github.gimme.gimmebot.core.command.medium

import com.github.gimme.gimmebot.core.command.CommandSender

/**
 * Can handle command output (response).
 */
interface CommandOutputHandler<in R> {

    /** Sends the given [response] to the given command [commandSender]. */
    fun respond(commandSender: CommandSender, response: R)
}
