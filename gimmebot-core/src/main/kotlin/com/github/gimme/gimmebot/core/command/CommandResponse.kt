package com.github.gimme.gimmebot.core.command

/**
 * Represents a response after executing a command with a [message] and [status] to be communicated back to the sender.
 */
class CommandResponse(
    private val message: String,
    private val status: Status = Status.SUCCESS,
) {
    /** The status of the command execution. */
    enum class Status {
        /** The command execution was successful. */
        SUCCESS,

        /** The command execution failed. */
        FAIL,
    }

    /** Sends this response to the given [receiver]. */
    fun send(receiver: MessageReceiver) {
        receiver.sendMessage(message)
    }
}
