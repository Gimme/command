package com.github.gimme.gimmebot.core.command

/**
 * Represents a command with base functionality.
 *
 * @param T the response type
 */
abstract class BaseCommand<out T>(
    final override val name: String,
    final override val parent: Command<*>? = null,
    final override var aliases: Set<String> = setOf(),
    override var summary: String = "",
    override var description: String = "",
    override var usage: String = "",
    override var parameters: CommandParameterSet = CommandParameterSet(),
) : Command<T> {

    @JvmOverloads
    constructor(name: String, parent: Command<*>? = null) : this(name, parent, setOf())

    override fun hashCode(): Int = id.hashCode()
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BaseCommand<*>

        if (id != other.id) return false

        return true
    }
}
