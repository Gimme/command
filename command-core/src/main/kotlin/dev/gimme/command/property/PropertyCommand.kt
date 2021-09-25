package dev.gimme.command.property

import dev.gimme.command.BaseCommand
import dev.gimme.command.node.CommandNode

/**
 * TODO
 *
 * @param R the type of the result of the command
 */
abstract class PropertyCommand<out R>(
    name: String,
    parent: CommandNode? = null,
    aliases: Set<String> = setOf(),
    description: String = "",
    detailedDescription: String? = null,
) : BaseCommand<R>(
    name = name,
    parent = parent,
    aliases = aliases,
    detailedDescription = detailedDescription,
    description = description,
) {

    @JvmOverloads
    constructor(name: String, parent: CommandNode? = null) : this(name, parent, setOf())
}

