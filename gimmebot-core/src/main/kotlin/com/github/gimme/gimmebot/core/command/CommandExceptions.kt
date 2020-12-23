package com.github.gimme.gimmebot.core.command


/** An argument has the wrong format. */
val INVALID_ARGUMENT_ERROR = CommandException("INVALID_ARGUMENT", "Invalid argument")

/** The command does not accept the type of the current command sender. */
val INCOMPATIBLE_SENDER_ERROR = CommandException("INCOMPATIBLE_SENDER", "You cannot use that command")

/** Too few arguments supplied with the command. */
val TOO_FEW_ARGUMENTS_ERROR = CommandException("TOO_FEW_ARGUMENTS", "Too few arguments")

/** Too many arguments supplied with the command. */
val TOO_MANY_ARGUMENTS_ERROR = CommandException("TOO_MANY_ARGUMENTS", "Too many arguments")
