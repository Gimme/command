package com.github.gimme.gimmebot.core.command

/**
 * Represents an executable command.
 * @property name The name of this command. Used as the main identifier.
 */
interface Command {
    val name: String

    /** Executes this command as the given [commandSender] and returns an optional response. */
    fun execute(commandSender: CommandSender): CommandResponse?
}
