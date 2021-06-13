package dev.gimme.gimmeapi.command

import dev.gimme.gimmeapi.command.exception.CommandException
import dev.gimme.gimmeapi.command.exception.ErrorCode
import dev.gimme.gimmeapi.command.node.BaseCommandNode
import dev.gimme.gimmeapi.command.node.CommandNode
import dev.gimme.gimmeapi.command.parameter.CommandParameter
import dev.gimme.gimmeapi.command.parameter.CommandParameterSet
import dev.gimme.gimmeapi.command.sender.CommandSender
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

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
    override val senderTypes: Set<KClass<out CommandSender>>? = null,
) : BaseCommandNode(name, parent, aliases), Command<T> {

    @JvmOverloads
    constructor(name: String, parent: CommandNode? = null) : this(name, parent, setOf())

    final override fun execute(commandSender: CommandSender, args: Map<CommandParameter, Any?>): T {
        if (senderTypes?.any { commandSender::class.isSubclassOf(it) } == false) throw ErrorCode.INCOMPATIBLE_SENDER.createException()

        return executeCommand(commandSender, args)
    }

    /**
     * @see execute
     */
    @Throws(CommandException::class)
    protected abstract fun executeCommand(commandSender: CommandSender, args: Map<CommandParameter, Any?>): T
}
