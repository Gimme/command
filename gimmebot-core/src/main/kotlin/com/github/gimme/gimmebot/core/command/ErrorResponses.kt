package com.github.gimme.gimmebot.core.command


/** An argument has the wrong format. */
val INVALID_ARGUMENT_ERROR = error("Invalid argument")

/** The command does not accept the type of the current command sender. */
val INCOMPATIBLE_SENDER_ERROR = error("You cannot use that command")

/** Too few arguments supplied with the command. */
val TOO_FEW_ARGUMENTS_ERROR = error("Too few arguments")

/** Too many arguments supplied with the command. */
val TOO_MANY_ARGUMENTS_ERROR = error("Too many arguments")


private fun error(message: String) = CommandResponse(message, CommandResponse.Status.ERROR)
