package dev.gimme.command

import dev.gimme.command.node.CommandNode
import dev.gimme.command.parameter.CommandParameter
import dev.gimme.command.parameter.CommandParameterSet
import dev.gimme.command.sender.CommandSender

val DUMMY_COMMAND_SENDER = object : CommandSender {
    override val name = "dummy"

    override fun sendMessage(message: String) {}
}

val DUMMY_COMMAND = object : DefaultBaseCommand("test") {}

open class DefaultBaseCommand(
    override val name: String,
    override val parent: CommandNode? = null,
) : Command<Any> {

    override fun execute(commandSender: CommandSender, args: Map<CommandParameter, Any?>): Any = Unit
    override var description = ""
    override var detailedDescription: String? = null
    override val usage = ""
    override val parameters = CommandParameterSet()
    override var aliases = setOf<String>()
    override val subcommands: MutableMap<String, CommandNode> = mutableMapOf()
}
