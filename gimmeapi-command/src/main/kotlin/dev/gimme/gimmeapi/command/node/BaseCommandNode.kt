package dev.gimme.gimmeapi.command.node

import dev.gimme.gimmeapi.command.BaseCommand

/**
 * A base implementation of command node with useful "hashCode" and "equals" methods.
 */
open class BaseCommandNode(
    final override val name: String,
    final override val parent: CommandNode? = null,
    final override var aliases: Set<String> = setOf(),
) : CommandNode {

    @JvmOverloads
    constructor(name: String, parent: CommandNode? = null) : this(name, parent, setOf())

    init {
        (aliases + name).forEach {
            require(it.isNotEmpty()) { "Name cannot be empty" }
            require(!it.contains(" ")) { "Name cannot contain spaces: \"$it\"" }
        }
    }

    override fun hashCode(): Int = id.hashCode()
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BaseCommand<*>

        if (id != other.id) return false

        return true
    }
}
