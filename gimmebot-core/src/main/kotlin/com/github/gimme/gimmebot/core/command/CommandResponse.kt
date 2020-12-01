package com.github.gimme.gimmebot.core.command

/**
 * Represents a response after executing a command with a [message] and [status] to be communicated back to the sender.
 *
 * @property message the message to be sent back to the command sender
 */
data class CommandResponse(
    val message: String,
    val status: Status = Status.SUCCESS,
) {
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

        /** The command execution failed. */
        FAIL,

        /** The command could not be called. */
        ERROR,
    }

    /** Sends this response to the given [receiver]. */
    fun sendTo(receiver: MessageReceiver) {
        receiver.sendMessage(message)
    }
}
