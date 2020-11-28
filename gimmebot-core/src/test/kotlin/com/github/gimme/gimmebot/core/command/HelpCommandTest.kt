package com.github.gimme.gimmebot.core.command

import com.github.gimme.gimmebot.core.command.manager.SimpleCommandManager
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class HelpCommandTest {

    @Test
    fun `should return list of commands`() {
        val commandManager = SimpleCommandManager()

        commandManager.registerCommand(DummyCommand("one"))
        commandManager.registerCommand(DummyCommand("two"))
        commandManager.registerCommand(DummyCommand("three"))

        val response: CommandResponse? = HelpCommand(commandManager).execute(DUMMY_CONSOLE_COMMAND_SENDER, listOf())

        assertNotNull(response)

        val message = response!!.message
        assertTrue(message.contains("one"))
        assertTrue(message.contains("two"))
        assertTrue(message.contains("three"))
    }

    private class DummyCommand(name: String) : BaseCommand(name)
}
