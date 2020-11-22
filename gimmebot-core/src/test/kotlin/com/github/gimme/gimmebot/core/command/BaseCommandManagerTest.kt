package com.github.gimme.gimmebot.core.command

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.function.Executable

class BaseCommandManagerTest {

    private val commandManager: CommandManager = object : BaseCommandManager() {}
    private val testCommand: Command = object : BaseCommand("test") {
        override fun execute(commandSender: CommandSender): CommandResponse? {
            return null
        }
    }
    private val dummyCommandSender: CommandSender = object : CommandSender {
        override fun sendMessage(message: String) {}
    }

    @Test
    fun `should register command`() {
        commandManager.registerCommand(testCommand)

        assertAll(
            Executable { assertEquals(testCommand, commandManager.getCommand("test")) },
            Executable { assertNull(commandManager.getCommand("test726")) },
        )
    }

    @Test
    fun `should execute command`() {
        var executed = false

        val command: Command = object : BaseCommand("executeTest") {
            override fun execute(commandSender: CommandSender): CommandResponse? {
                executed = true
                return null
            }
        }

        commandManager.registerCommand(command)
        commandManager.executeCommand(dummyCommandSender, command)

        assertTrue(executed)
    }
}
