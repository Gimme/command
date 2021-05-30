package com.github.gimme.gimmebot.core.command

import com.github.gimme.gimmebot.core.command.parameter.CommandParameterSet
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
    override val name: String,
    override val parent: Command<*>? = null,
) : Command<Any> {
    override var aliases: Set<String> = setOf()
    override var summary: String = ""
    override var description: String = ""
    override var usage = ""
    override var parameters = CommandParameterSet()

    override fun execute(commandSender: CommandSender, args: List<String>) {}
}
