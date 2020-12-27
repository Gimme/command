package com.github.gimme.gimmebot.core.command.medium

import com.github.gimme.gimmebot.core.command.manager.CommandManager

/**
 * Represents a command input medium with base functionality.
 *
 * @param R the response type
 */
abstract class BaseCommandMedium<R>(override var commandManager: CommandManager<R>) : CommandMedium<R> {

    override fun install() {
        onInstall()
    }

    /** Performs logic when installed. */
    protected abstract fun onInstall()
}
