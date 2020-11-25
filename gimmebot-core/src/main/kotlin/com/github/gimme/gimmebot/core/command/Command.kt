package com.github.gimme.gimmebot.core.command

/**
 * Represents an executable command.
 *
 * @property name The name of this command. Used as the main identifier. Can be multiple words.
 */
interface Command {
    val name: String

    /** Executes this command as the given [commandSender] with the given [args] and returns an optional response. */
    fun execute(commandSender: CommandSender, args: List<String>): CommandResponse?
}
