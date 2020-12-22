package com.github.gimme.gimmebot.core.command


/** An argument has the wrong format. */
fun <T> INVALID_ARGUMENT_ERROR() = error<T>("Invalid argument")

/** The command does not accept the type of the current command sender. */
fun <T> INCOMPATIBLE_SENDER_ERROR() = error<T>("You cannot use that command")

/** Too few arguments supplied with the command. */
fun <T> TOO_FEW_ARGUMENTS_ERROR() = error<T>("Too few arguments")

/** Too many arguments supplied with the command. */
fun <T> TOO_MANY_ARGUMENTS_ERROR() = error<T>("Too many arguments")


private fun <T> error(message: String) = CommandResponse<T>(message, CommandResponse.Status.ERROR)
