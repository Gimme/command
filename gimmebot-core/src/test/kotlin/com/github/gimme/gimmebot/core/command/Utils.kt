package com.github.gimme.gimmebot.core.command

import com.github.gimme.gimmebot.core.command.sender.CommandSender

val DUMMY_COMMAND_SENDER = object : CommandSender {
    override val name = ""

    override fun sendMessage(message: String) {}
}

val DUMMY_COMMAND = object : DefaultBaseCommand("test") {
    override val usage = ""
    override val parameters = CommandParameterSet(listOf())

    override fun execute(commandSender: CommandSender, args: List<String>) {}
}

open class DefaultBaseCommand(name: String) : BaseCommand<Any>(name) {
    override val usage = ""
    override val parameters = CommandParameterSet(listOf())

    override fun execute(commandSender: CommandSender, args: List<String>) {}
}
