package com.github.gimme.gimmebot.core.command

/**
 * Represents a command with base functionality.
 *
 * @param T the response type
 */
abstract class BaseCommand<out T> @JvmOverloads constructor(
    override val name: String,
    override var aliases: Set<String> = setOf(),
    override var summary: String = "",
    override var description: String = "",
) : Command<T> {

    override fun hashCode(): Int = name.hashCode()
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BaseCommand<*>

        if (name != other.name) return false

        return true
    }
}
