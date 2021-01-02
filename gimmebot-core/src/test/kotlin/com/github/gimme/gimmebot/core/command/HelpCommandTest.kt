package com.github.gimme.gimmebot.core.command

import com.github.gimme.gimmebot.core.command.executor.CommandExecutor
import com.github.gimme.gimmebot.core.command.medium.TextCommandMedium
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class HelpCommandTest {

    @Test
    fun `should return list of commands`() {
        val commandMedium = object : TextCommandMedium() {
            override fun onEnable() {}
        }

        commandMedium.commandManager.registerCommand(DummyCommand("one"))
        commandMedium.commandManager.registerCommand(DummyCommand("two"))
        commandMedium.commandManager.registerCommand(DummyCommand("three"))

        val response = HelpCommand(commandMedium).execute(DUMMY_COMMAND_SENDER, listOf())

        assertTrue(response.size == 3)
        assertTrue(response[0].name == "one")
        assertTrue(response[1].name == "two")
        assertTrue(response[2].name == "three")
    }

    private class DummyCommand(name: String) : DefaultBaseCommand(name) {
        @CommandExecutor
        fun execute() {}
    }
}
