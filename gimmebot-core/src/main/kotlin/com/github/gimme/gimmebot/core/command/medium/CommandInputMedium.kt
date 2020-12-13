package com.github.gimme.gimmebot.core.command.medium

import com.github.gimme.gimmebot.core.command.manager.CommandManager

/**
 * Represents a medium through which commands can be sent.
 *
 * For example a chat box or a command line interface.
 */
interface CommandInputMedium {

    /** Installs this command input medium on the given [commandManager]. */
    fun install(commandManager: CommandManager)
}
