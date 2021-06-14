package dev.gimme.gimmeapi.command

import dev.gimme.gimmeapi.command.parameter.CommandParameter
import dev.gimme.gimmeapi.command.parameter.CommandParameterSet
import dev.gimme.gimmeapi.command.sender.CommandSender
import kotlin.reflect.KClass

val DUMMY_COMMAND_SENDER = object : CommandSender {
    override val name = "dummy"

    override fun sendMessage(message: String) {}
}

val DUMMY_COMMAND = object : DefaultBaseCommand("test") {
    override var usage = ""
    override var parameters = CommandParameterSet(listOf())

    override fun executeBy(commandSender: CommandSender, args: Map<CommandParameter, Any?>) {}
}

open class DefaultBaseCommand(
    override val name: String,
    override val parent: Command<*>? = null,
) : Command<Any> {
    override var aliases: Set<String> = setOf()
    override var summary: String = ""
    override var description: String = ""
    override var usage = ""
    override var parameters = CommandParameterSet()
    override var senderTypes: Set<KClass<out CommandSender>>? = null

    override fun executeBy(commandSender: CommandSender, args: Map<CommandParameter, Any?>) {}
}
