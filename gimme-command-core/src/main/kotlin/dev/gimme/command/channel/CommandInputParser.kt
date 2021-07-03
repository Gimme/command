package dev.gimme.command.channel

import dev.gimme.command.sender.CommandSender

/**
 * Can handle command input.
 */
interface CommandInputParser {

    /** Parses the specified [input] as the given [sender] and returns if the [input] matched a command. */
    fun parseInput(sender: CommandSender, input: String): Boolean
}
