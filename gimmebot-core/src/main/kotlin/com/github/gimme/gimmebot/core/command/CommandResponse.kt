package com.github.gimme.gimmebot.core.command

/**
 * Represents a response after executing a command with a [message] and [status] to be communicated back to the sender.
 *
 * @property message the message to be sent back to the command sender
 */
class CommandResponse(
    val message: String,
    private val status: Status = Status.SUCCESS,
) {
    /** The status of a command execution. */
    enum class Status {
        /** The command execution was successful. */
        SUCCESS,

        /** The command execution failed. */
        FAIL,
    }

    /** Sends this response to the given [receiver]. */
    fun sendTo(receiver: MessageReceiver) {
        receiver.sendMessage(message)
    }
}
