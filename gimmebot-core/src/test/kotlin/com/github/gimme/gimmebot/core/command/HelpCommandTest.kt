package com.github.gimme.gimmebot.core.command

import com.github.gimme.gimmebot.core.command.executor.CommandExecutor
import com.github.gimme.gimmebot.core.command.medium.TextCommandMedium
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class HelpCommandTest {

    @Test
    fun `should return list of commands`() {
        val commandMedium = object : TextCommandMedium(false) {
            override fun onEnable() {}
            override fun onDisable() {}
        }

        commandMedium.commandManager.registerCommand(DummyCommand("one"))
        commandMedium.commandManager.registerCommand(DummyCommand("two"))
        commandMedium.commandManager.registerCommand(DummyCommand("three"))

        val response = HelpCommand(commandMedium).execute(DUMMY_COMMAND_SENDER, listOf())

        assertEquals( 3, response.size)
        assertEquals("one", response[0].name)
        assertEquals("two", response[1].name)
        assertEquals("three", response[2].name)
    }

    private class DummyCommand(name: String) : DefaultBaseCommand(name) {
        @CommandExecutor
        fun execute() {
        }
    }
}
