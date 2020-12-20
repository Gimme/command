package com.github.gimme.gimmebot.core.command

/**
 * Represents a response after executing a command with a [message], [status] and [body] to be communicated back to the sender.
 *
 * @property message the message to be sent back to the command sender, or null if no message to be sent
 * @property status  shows if the command execution was successful or not
 * @property body    core response data containing the result of the command
 */
data class CommandResponse(
    val message: String? = null,
    val status: Status = Status.SUCCESS,
    val body: Any? = null,
) {

    constructor(message: String?, body: Any?) : this(message, Status.SUCCESS, body)

    constructor(body: Any?) : this(null, Status.SUCCESS, body)

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
