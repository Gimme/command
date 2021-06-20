package dev.gimme.gimmeapi.command

import dev.gimme.gimmeapi.command.parameter.CommandParameter
import dev.gimme.gimmeapi.command.parameter.CommandParameterSet
import dev.gimme.gimmeapi.command.sender.CommandSender
import kotlin.reflect.KClass

val DUMMY_COMMAND_SENDER = object : CommandSender {
    override val name = "dummy"

    override fun sendMessage(message: String) {}
}

val DUMMY_COMMAND = object : DefaultBaseCommand("test") {}

open class DefaultBaseCommand(
    name: String,
    parent: Command<*>? = null,
) : BaseCommand<Any>(
    name,
    parent,
) {

    override fun execute(commandSender: CommandSender, args: Map<CommandParameter, Any?>) {}
}
