package com.github.gimme.gimmebot.core.command

val DUMMY_COMMAND_SENDER = object : CommandSender {
    override fun sendMessage(message: String) {}
}

val DUMMY_COMMAND: Command = object : BaseCommand("test") {
    override fun execute(commandSender: CommandSender, args: List<String>): CommandResponse? {
        return null
    }
}

val DUMMY_RESPONSE = CommandResponse("")
