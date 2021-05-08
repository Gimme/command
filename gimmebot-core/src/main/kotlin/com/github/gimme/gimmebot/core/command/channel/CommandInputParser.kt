package com.github.gimme.gimmebot.core.command.channel

import com.github.gimme.gimmebot.core.command.sender.CommandSender

/**
 * Can handle command input.
 */
interface CommandInputParser {

    /** Parses the specified [input] as the given [sender] and returns if the [input] matched a command. */
    fun parseInput(sender: CommandSender, input: String): Boolean
}
