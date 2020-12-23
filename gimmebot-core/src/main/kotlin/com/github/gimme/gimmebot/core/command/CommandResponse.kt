package com.github.gimme.gimmebot.core.command

/**
 * Represents a response after executing a command with a [message], [status] and [body] to be communicated back to the
 * sender. If the command execution failed, an [error] is included with the reason.
 *
 * @param T the type of the response body
 * @property message the message to be sent back to the command sender, or null if no message to be sent
 * @property status  shows if the command execution was successful or not
 * @property body    the response data containing the result of the command, or null if the command did not return any
 * @property error   a command error if the command returned unsuccessfully, else null
 * data
 */
data class CommandResponse<out T>(
    val message: String? = null,
    val status: Status = Status.SUCCESS,
    val body: T? = null,
    val error: CommandException? = null
) {

    constructor(message: String?, body: T?) : this(message, Status.SUCCESS, body)

    constructor(body: T?) : this(null, Status.SUCCESS, body)

    /** Sends this response's message to the given [receiver]. */
    fun sendTo(receiver: MessageReceiver) {
        message?.let { receiver.sendMessage(it) }
    }

    /** The status of a command execution. */
    enum class Status {
        /** The command execution was successful. */
        SUCCESS,

        /** The command could not be called. */
        ERROR,
    }
}
