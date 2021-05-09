package com.github.gimme.gimmebot.core.command

/**
 * Represents a command with base functionality.
 *
 * @param T      the response type
 * @param name   the command name
 * @param parent the parent command, or null if no parent
 */
abstract class BaseCommand<out T>(
    final override val name: String,
    final override val parent: Command<T>? = null,
) : Command<T> {

    final override val id: String = "${parent?.let { "${it.id}$groupDelimiter" } ?: ""}$name"

    override fun hashCode(): Int = id.hashCode()
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BaseCommand<*>

        if (id != other.id) return false

        return true
    }
}
