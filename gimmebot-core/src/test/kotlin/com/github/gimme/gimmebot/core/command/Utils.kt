package com.github.gimme.gimmebot.core.command

val DUMMY_CONSOLE_COMMAND_SENDER = object : CommandSender {
    override val medium: CommandSender.Medium
        get() = CommandSender.Medium.CONSOLE

    override fun sendMessage(message: String) {}
}

val DUMMY_CHAT_COMMAND_SENDER = object : CommandSender {
    override val medium: CommandSender.Medium
        get() = CommandSender.Medium.CHAT

    override fun sendMessage(message: String) {}
}

val DUMMY_COMMAND: Command = object : BaseCommand("test") {
    override fun execute(commandSender: CommandSender, args: List<String>): CommandResponse? {
        return null
    }
}

val DUMMY_RESPONSE = CommandResponse("")
