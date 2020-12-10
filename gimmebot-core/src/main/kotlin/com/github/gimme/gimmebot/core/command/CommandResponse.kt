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

    companion object {
        /** An argument has the wrong format. */
        val INVALID_ARGUMENT = error("Invalid argument")

        /** The command does not accept the type of the current command sender. */
        val INCOMPATIBLE_SENDER = error("You cannot use that command")

        /** Too few arguments supplied with the command. */
        val TOO_FEW_ARGUMENTS = error("Too few arguments")

        /** Too many arguments supplied with the command. */
        val TOO_MANY_ARGUMENTS = error("Too many arguments")

        private fun error(message: String) = CommandResponse(message, Status.ERROR)
    }

    /** The status of a command execution. */
    enum class Status {
        /** The command execution was successful. */
        SUCCESS,

        /** The command could not be called. */
        ERROR,
    }

    /** Sends this response to the given [receiver]. */
    fun sendTo(receiver: MessageReceiver) {
        if (message != null) {
            receiver.sendMessage(message)
        }
    }
}
