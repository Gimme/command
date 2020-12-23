package com.github.gimme.gimmebot.core.command

/**
 * Represents an executable command.
 *
 * @param T the response type
 * @property name the name of this command. Used as the main identifier. Can be multiple words.
 * @property usage information of how to use the command
 */
interface Command<out T> {
    val name: String
    val usage: String

    /**
     * Executes this command as the given [commandSender] with the given [args] and returns the response.
     *
     * @throws CommandException if the command execution was unsuccessful
     */
    @Throws(CommandException::class)
    fun execute(commandSender: CommandSender, args: List<String>): T
}
