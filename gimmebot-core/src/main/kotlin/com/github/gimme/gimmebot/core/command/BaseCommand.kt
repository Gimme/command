package com.github.gimme.gimmebot.core.command

/**
 * Represents a command with base functionality.
 */
abstract class BaseCommand(name: String) : Command {
    override val name: String = name.toLowerCase()

    //TODO
}
