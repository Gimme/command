package com.github.gimme.gimmebot.core.command

/**
 * An exception to be thrown when something goes wrong during command execution.
 *
 * @property code an identifier for the type of the error
 */
class CommandException(
    val code: String,
    message: String,
) : RuntimeException(message) {

    /**
     * Creates a new empty [CommandResponse] with this error.
     */
    fun <T> response(): CommandResponse<T> {
        return CommandResponse(error = this)
    }
}
