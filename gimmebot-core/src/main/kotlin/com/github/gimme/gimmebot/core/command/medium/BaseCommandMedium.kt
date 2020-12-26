package com.github.gimme.gimmebot.core.command.medium

import com.github.gimme.gimmebot.core.command.manager.commandcollection.CommandCollection

/**
 * Represents a command input medium with base functionality.
 */
abstract class BaseCommandMedium(override var commandCollection: CommandCollection) : CommandMedium {

    override fun install() {
        onInstall()
    }

    /** Performs logic when installed. */
    protected abstract fun onInstall()
}
