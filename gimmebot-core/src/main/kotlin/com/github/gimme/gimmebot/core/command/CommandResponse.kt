package com.github.gimme.gimmebot.core.command

/**
 * Represents a response after executing a command with and [body] to be communicated back to the
 * sender. If the command execution failed, an [error] is included with the reason.
 *
 * @param T the type of the response body
 * @property body    the response data containing the result of the command, or null if the command did not return any
 * @property error   a command error if the command returned unsuccessfully, else null
 * data
 */
data class CommandResponse<out T>(
    val body: T? = null,
    val error: CommandException? = null,
) {

    /** Sends this response's message to the given [receiver]. */
    fun sendTo(receiver: MessageReceiver) {
        body?.let { receiver.sendMessage(it.toString()) }
    }
}
