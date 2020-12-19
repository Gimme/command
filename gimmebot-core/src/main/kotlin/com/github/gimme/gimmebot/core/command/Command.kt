package com.github.gimme.gimmebot.core.command

/**
 * Represents an executable command.
 *
 * @property name the name of this command. Used as the main identifier. Can be multiple words.
 * @property usage information of how to use the command
 */
interface Command {
    val name: String
    val usage: String

    /** Executes this command as the given [commandSender] with the given [args] and returns an optional response. */
    fun execute(commandSender: CommandSender, args: List<String>): CommandResponse?
}
