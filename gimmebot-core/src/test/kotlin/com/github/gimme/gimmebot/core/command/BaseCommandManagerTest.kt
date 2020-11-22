package com.github.gimme.gimmebot.core.command

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

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

        assertEquals(testCommand, commandManager.getCommand("testt"))
    }
}
