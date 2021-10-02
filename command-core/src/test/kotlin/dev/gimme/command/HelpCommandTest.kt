package dev.gimme.command

import dev.gimme.command.commands.HelpCommand
import dev.gimme.command.function.CommandFunction
import dev.gimme.command.function.FunctionCommand
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class HelpCommandTest {

    @Test
    fun `should return list of commands`() {
        val commands = listOf(DummyCommand("one"), DummyCommand("two"), DummyCommand("three"))

        val response = HelpCommand { commands }.execute(
            DUMMY_COMMAND_SENDER,
            mapOf()
        )

        assertEquals(3, response.size)
        assertEquals("one", response[0].name)
        assertEquals("two", response[1].name)
        assertEquals("three", response[2].name)
    }

    private class DummyCommand(name: String) : FunctionCommand<Any>(name) {
        @CommandFunction
        fun execute() {
        }
    }
}
