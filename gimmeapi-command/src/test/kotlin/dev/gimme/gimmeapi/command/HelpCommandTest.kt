package dev.gimme.gimmeapi.command

import dev.gimme.gimmeapi.command.commands.HelpCommand
import dev.gimme.gimmeapi.command.function.CommandExecutor
import dev.gimme.gimmeapi.command.channel.TextCommandChannel
import dev.gimme.gimmeapi.command.function.FunctionCommand
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

        val response = HelpCommand(commandChannel).executeBy(DUMMY_COMMAND_SENDER, mapOf())

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
