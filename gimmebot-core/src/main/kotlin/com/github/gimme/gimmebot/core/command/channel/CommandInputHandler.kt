package com.github.gimme.gimmebot.core.command.channel

import com.github.gimme.gimmebot.core.command.CommandSender

/**
 * Can handle command input.
 */
interface CommandInputHandler {

    /** Parses the specified [input] as the given [sender]. */
    fun parseInput(sender: CommandSender, input: String)
}
