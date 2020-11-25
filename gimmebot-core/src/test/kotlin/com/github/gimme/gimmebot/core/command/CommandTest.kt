package com.github.gimme.gimmebot.core.command

abstract class CommandTest {

    protected val commandManager = SimpleCommandManager()

    protected class DummyCommand(name: String) : BaseCommand(name) {
        override fun execute(commandSender: CommandSender, args: List<String>): CommandResponse? {
            return null
        }
    }

    protected class DummyCommandSender(private val accept: (message: String) -> Unit) : CommandSender {
        override val medium: CommandSender.Medium = CommandSender.Medium.CONSOLE

        override fun sendMessage(message: String) {
            accept(message)
        }
    }
}
