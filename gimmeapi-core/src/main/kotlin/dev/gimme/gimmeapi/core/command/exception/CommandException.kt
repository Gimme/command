package dev.gimme.gimmeapi.core.command.exception

/**
 * An exception to be thrown when something goes wrong during command execution.
 *
 * @property code    an identifier for the type of the error
 * @property message the message explaining what went wrong
 */
open class CommandException(
    val code: String,
    override val message: String,
) : RuntimeException(message) {

    constructor(errorCode: ErrorCode) : this(errorCode.code, errorCode.message)
}
