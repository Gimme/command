package com.github.gimme.gimmebot.core.command

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class HelpCommandTest : CommandTest() {

    @Test
    fun `should return list of commands`() {
        commandManager.registerCommand(DummyCommand("one"))
        commandManager.registerCommand(DummyCommand("two"))
        commandManager.registerCommand(DummyCommand("three"))

        var actual = ""

        val commandSender: CommandSender = DummyCommandSender { message -> actual = message }
        val response: CommandResponse? = HelpCommand(commandManager).execute(commandSender, listOf())
        response?.sendTo(commandSender)

        assertNotNull(response)
        assertTrue(actual.contains("one"))
        assertTrue(actual.contains("two"))
        assertTrue(actual.contains("three"))
    }
}
