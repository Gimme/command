package com.github.gimme.gimmebot.core.command

/**
 * An identifier for a type of command error.
 *
 * @property message a message explaining the error
 */
enum class ErrorCode(val message: String) {

    /** The fetched command does not exist */
    NOT_A_COMMAND("Not a command"),

    /** An argument has the wrong format. */
    INVALID_ARGUMENT("Invalid argument"),

    /** The command does not accept the type of the current command sender. */
    INCOMPATIBLE_SENDER("You cannot use that command"),

    /** Too few arguments supplied with the command. */
    TOO_FEW_ARGUMENTS("Too few arguments"),

    /** Too many arguments supplied with the command. */
    TOO_MANY_ARGUMENTS("Too many arguments");

    /** Returns the identifier code. */
    fun code(): String = name

    /**
     * Creates a [CommandException] with this error code and an optional [context] object to be included in the message.
     *
     * For example: the message "Invalid argument" or "Invalid argument: 43" with 43 being the context in this case.
     */
    fun createException(context: Any? = null): CommandException =
        CommandException(code(), message + (context?.let { ": $it" } ?: ""))
}
