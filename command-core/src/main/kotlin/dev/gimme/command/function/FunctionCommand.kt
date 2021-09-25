package dev.gimme.command.function

import dev.gimme.command.BaseCommand
import dev.gimme.command.node.CommandNode

/**
 * Represents an easy-to-set-up command with automatic generation of some properties derived from a member function
 * marked with @[CommandFunction].
 *
 * If a method in this is marked with @[CommandFunction], the command's [parameters] and [usage] are automatically
 * derived from it, and it gets called called when the command is executed.
 *
 * @param T the response type
 */
abstract class FunctionCommand<out T>(
    name: String,
    parent: CommandNode? = null,
    aliases: Set<String> = setOf(),
    description: String = "",
    detailedDescription: String? = null,
) : BaseCommand<T>(
    name = name,
    parent = parent,
    aliases = aliases,
    description = description,
    detailedDescription = detailedDescription,
) {

    @JvmOverloads
    constructor(name: String, parent: CommandNode? = null) : this(name, parent, setOf())
}
