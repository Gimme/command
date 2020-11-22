package com.github.gimme.gimmebot.core.command

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.function.Executable

class BaseCommandManagerTest {
    private val commandManager: CommandManager = object : BaseCommandManager() {}
    private val testCommand: Command = object : BaseCommand("test") {
        override fun execute(): String? {
            return null
        }
    }

    @Test
    fun `registerCommand should add command`() {
        commandManager.registerCommand(testCommand)

        assertAll(
            Executable { assertEquals(testCommand, commandManager.getCommand("test")) },
            Executable { assertNull(commandManager.getCommand("test726")) },
        )
    }
}
