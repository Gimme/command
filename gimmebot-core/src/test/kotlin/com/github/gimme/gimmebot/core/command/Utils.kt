package com.github.gimme.gimmebot.core.command

import com.github.gimme.gimmebot.core.command.sender.CommandSender

val DUMMY_COMMAND_SENDER = object : CommandSender {
    override val name = ""

    override fun sendMessage(message: String) {}
}

val DUMMY_COMMAND = object : DefaultBaseCommand("test") {
    override var usage = ""
    override var parameters = CommandParameterSet(listOf())

    override fun execute(commandSender: CommandSender, args: List<String>) {}
}

open class DefaultBaseCommand(
    name: String,
    parent: Command<*>? = null,
) : BaseCommand<Any>(
    name = name,
    parent = parent,
) {
    override var usage = ""
    override var parameters = CommandParameterSet(listOf())

    override fun execute(commandSender: CommandSender, args: List<String>) {}
}
