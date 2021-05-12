package com.github.gimme.gimmebot.core.command

/**
 * Represents a command with base functionality.
 *
 * @param T the response type
 */
abstract class BaseCommand<out T>(override val name: String) : Command<T> {

    override fun hashCode(): Int = name.hashCode()
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BaseCommand<*>

        if (name != other.name) return false

        return true
    }
}
