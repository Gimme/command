package dev.gimme.gimmeapi.boot.command

import dev.gimme.gimmeapi.boot.command.commands.HelpCommand
import dev.gimme.gimmeapi.boot.command.executor.CommandExecutor
import dev.gimme.gimmeapi.command.channel.TextCommandChannel
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class HelpCommandTest {

    @Test
    fun `should return list of commands`() {
        val commandChannel = object : TextCommandChannel() {
            override fun onEnable() {}
            override fun onDisable() {}
        }

        commandChannel.commandManager.registerCommand(DummyCommand("one"))
        commandChannel.commandManager.registerCommand(DummyCommand("two"))
        commandChannel.commandManager.registerCommand(DummyCommand("three"))

        val response = HelpCommand(commandChannel).execute(DUMMY_COMMAND_SENDER, mapOf())

        assertEquals( 3, response.size)
        assertEquals("one", response[0].name)
        assertEquals("two", response[1].name)
        assertEquals("three", response[2].name)
    }

    private class DummyCommand(name: String) : FunctionCommand<Any>(name) {
        @CommandExecutor
        fun execute() {
        }
    }
}
