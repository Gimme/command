package com.github.gimme.gimmebot.core.command

/**
 * Represents a command with base functionality.
 *
 * @param T      the response type
 * @param name   the non-empty name of this command
 * @param parent the name of this command's optional parent
 */
abstract class BaseCommand<out T>(name: String, parent: String? = null) : Command<T> {

    final override val name: String

    init {
        require(name.isNotEmpty())
        this.name = (parent?.let { "$parent." } ?: "") + name.toLowerCase()
    }

    protected constructor(name: String, parent: Command<T>) : this(name, parent.name)

    override fun hashCode(): Int = group.hashCode()
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BaseCommand<*>

        if (name != other.name) return false

        return true
    }
}
