package com.github.gimme.gimmebot.core.command

val DUMMY_COMMAND_SENDER = object : CommandSender {
    override val name: String
        get() = ""

    override fun sendMessage(message: String) {}
}

val DUMMY_COMMAND: Command<Any> = object : BaseCommand<Any>("test") {
    override fun execute(commandSender: CommandSender, args: List<String>) {}
}

const val DUMMY_RESPONSE = ""
