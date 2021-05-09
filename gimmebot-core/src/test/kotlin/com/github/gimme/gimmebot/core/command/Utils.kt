package com.github.gimme.gimmebot.core.command

import com.github.gimme.gimmebot.core.command.sender.CommandSender
import com.github.gimme.gimmebot.core.common.grouped.Grouped

val DUMMY_COMMAND_SENDER = object : CommandSender {
    override val name = ""

    override fun sendMessage(message: String) {}
}

val DUMMY_COMMAND = object : DefaultBaseCommand("test") {
    override var usage = ""
    override var parameters = CommandParameterSet(listOf())

    override fun execute(commandSender: CommandSender, args: List<String>) {}
}

open class DefaultBaseCommand(name: String) : BaseCommand<Any>(name) {
    override var usage = ""
    override var parameters = CommandParameterSet(listOf())

    override fun execute(commandSender: CommandSender, args: List<String>) {}
}

data class GroupedId(override val id: String) : Grouped
