package com.github.gimme.gimmebot.core.command

import com.github.gimme.gimmebot.core.command.node.BaseCommandNode
import com.github.gimme.gimmebot.core.command.node.CommandNode
import com.github.gimme.gimmebot.core.command.parameter.CommandParameterSet

/**
 * A base implementation of command with useful "hashCode" and "equals" methods.
 *
 * @param T the response type
 */
abstract class BaseCommand<out T>(
    name: String,
    parent: CommandNode? = null,
    aliases: Set<String> = setOf(),
    override var summary: String = "",
    override var description: String = "",
    override var usage: String = "",
    override var parameters: CommandParameterSet = CommandParameterSet(),
) : BaseCommandNode(name, parent, aliases), Command<T> {

    @JvmOverloads
    constructor(name: String, parent: CommandNode? = null) : this(name, parent, setOf())
}
