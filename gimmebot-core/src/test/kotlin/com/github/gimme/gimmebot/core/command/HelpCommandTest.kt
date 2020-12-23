package com.github.gimme.gimmebot.core.command

import com.github.gimme.gimmebot.core.command.executor.CommandExecutor
import com.github.gimme.gimmebot.core.command.manager.commandcollection.CommandTree
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class HelpCommandTest {

    @Test
    fun `should return list of commands`() {
        val commandCollection = CommandTree()

        commandCollection.addCommand(DummyCommand("one"))
        commandCollection.addCommand(DummyCommand("two"))
        commandCollection.addCommand(DummyCommand("three"))

        val response: String? = HelpCommand(commandCollection).execute(DUMMY_COMMAND_SENDER, listOf())

        assertNotNull(response!!)

        val message = response.toString()

        assertTrue(message.contains("one"))
        assertTrue(message.contains("two"))
        assertTrue(message.contains("three"))
    }

    private class DummyCommand(name: String) : BaseCommand<Unit>(name) {
        @CommandExecutor
        fun execute() {}
    }
}
