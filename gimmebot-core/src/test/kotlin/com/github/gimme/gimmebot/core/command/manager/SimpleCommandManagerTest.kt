package com.github.gimme.gimmebot.core.command.manager

import com.github.gimme.gimmebot.core.command.sender.CommandSender
import com.github.gimme.gimmebot.core.command.DUMMY_COMMAND
import com.github.gimme.gimmebot.core.command.DUMMY_COMMAND_SENDER
import com.github.gimme.gimmebot.core.command.DefaultBaseCommand
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertIterableEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class SimpleCommandManagerTest {

    private val commandManager: CommandManager<Any?> = SimpleCommandManager { it }

    @Test
    fun `should register command`() {
        commandManager.registerCommand(DUMMY_COMMAND)

        assertAll(
            { assertNull(commandManager.getCommand("test726")) },
            { assertEquals(DUMMY_COMMAND, commandManager.getCommand("test")) },
        )
    }

    @Test
    fun `register command with same name should overwrite`() {
        commandManager.registerCommand(object : DefaultBaseCommand("test") {})
        commandManager.registerCommand(DUMMY_COMMAND)

        assertEquals(DUMMY_COMMAND, commandManager.getCommand("test"))
    }

    @ParameterizedTest
    @CsvSource(
        "test, test",
        "Test, test",
        "test, TEST",
        "Test, Test",
        "test1 test2, test1 test2",
    )
    fun `should execute command`(commandName: String, inputCommandName: String) {
        var executed = false

        val command = object : DefaultBaseCommand(commandName) {
            override fun execute(commandSender: CommandSender, args: List<String>) {
                executed = true
            }
        }

        commandManager.registerCommand(command)
        commandManager.executeCommand(DUMMY_COMMAND_SENDER, inputCommandName)

        assertTrue(executed)
    }

    @Test
    fun `should execute parent child commands separately`() {
        var parentExecuted = false
        var childExecuted = false

        val parentCommand = object : DefaultBaseCommand("parent") {
            override fun execute(commandSender: CommandSender, args: List<String>) {
                assertIterableEquals(listOf("a", "b"), args)
                parentExecuted = true
            }
        }
        val childCommand = object : DefaultBaseCommand("parent child") {
            override fun execute(commandSender: CommandSender, args: List<String>) {
                assertIterableEquals(listOf("x", "y"), args)
                childExecuted = true
            }
        }

        commandManager.registerCommand(parentCommand)
        commandManager.registerCommand(childCommand)
        commandManager.executeCommand(DUMMY_COMMAND_SENDER, "parent", listOf("a", "b"))
        commandManager.executeCommand(DUMMY_COMMAND_SENDER, "parent child", listOf("x", "y"))

        assertTrue(parentExecuted)
        assertTrue(childExecuted)

        parentExecuted = false
        childExecuted = false

        val commandManager2 = SimpleCommandManager { it }
        commandManager2.registerCommand(childCommand)
        commandManager2.registerCommand(parentCommand)
        commandManager2.executeCommand(DUMMY_COMMAND_SENDER, "parent child", listOf("x", "y"))
        commandManager2.executeCommand(DUMMY_COMMAND_SENDER, "parent", listOf("a", "b"))

        assertTrue(childExecuted)
        assertTrue(parentExecuted)
    }
}
