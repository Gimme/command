package com.github.gimme.gimmebot.core.command.manager

import com.github.gimme.gimmebot.core.command.BaseCommand
import com.github.gimme.gimmebot.core.command.CommandSender
import com.github.gimme.gimmebot.core.command.DUMMY_COMMAND
import com.github.gimme.gimmebot.core.command.DUMMY_COMMAND_SENDER
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertIterableEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.MethodSource

class SimpleCommandManagerTest {

    private val commandManager: CommandManager = SimpleCommandManager()

    @Test
    fun `should have a help command by default`() {
        assertNotNull(commandManager.getCommand("help"))
    }

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
        commandManager.registerCommand(object : BaseCommand<Unit>("test") {})
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
    fun `should execute command`(commandName: String, inputCommand: String) {
        var executed = false

        val command = object : BaseCommand<Any>(commandName) {
            override fun execute(commandSender: CommandSender, args: List<String>) {
                executed = true
            }
        }

        commandManager.registerCommand(command)
        commandManager.parseInput(DUMMY_COMMAND_SENDER, inputCommand)

        assertTrue(executed)
    }

    @Test
    fun `should execute parent child commands separately`() {
        var parentExecuted = false
        var childExecuted = false

        val parentCommand = object : BaseCommand<Any>("parent") {
            override fun execute(commandSender: CommandSender, args: List<String>) {
                assertIterableEquals(listOf("a", "b"), args)
                parentExecuted = true
            }
        }
        val childCommand = object : BaseCommand<Any>("parent child") {
            override fun execute(commandSender: CommandSender, args: List<String>) {
                assertIterableEquals(listOf("x", "y"), args)
                childExecuted = true
            }
        }

        commandManager.registerCommand(parentCommand)
        commandManager.registerCommand(childCommand)
        commandManager.parseInput(DUMMY_COMMAND_SENDER, "parent a b")
        commandManager.parseInput(DUMMY_COMMAND_SENDER, "parent child x y")

        assertTrue(parentExecuted)
        assertTrue(childExecuted)

        val commandManager2 = SimpleCommandManager()
        commandManager2.registerCommand(childCommand)
        commandManager2.registerCommand(parentCommand)
        commandManager2.parseInput(DUMMY_COMMAND_SENDER, "parent child x y")
        commandManager2.parseInput(DUMMY_COMMAND_SENDER, "parent a b")

        assertTrue(childExecuted)
        assertTrue(parentExecuted)
    }

    @ParameterizedTest
    @MethodSource("args")
    fun `should pass arguments`(input: String, expectedArgs: List<String>) {
        var actualArgs: List<String>? = null

        val command = object : BaseCommand<Any>("c") {
            override fun execute(commandSender: CommandSender, args: List<String>) {
                actualArgs = args
            }
        }

        commandManager.registerCommand(command)
        commandManager.parseInput(DUMMY_COMMAND_SENDER, input)

        assertNotNull(actualArgs)
        assertIterableEquals(expectedArgs, actualArgs)
    }

    companion object {
        @JvmStatic
        fun args() = listOf(
            Arguments.of("c", emptyList<String>()),
            Arguments.of("c ", listOf("")),
            Arguments.of("c   one", listOf("", "", "one")),
            Arguments.of("c one", listOf("one")),
            Arguments.of("c one two", listOf("one", "two")),
            Arguments.of("c one two three", listOf("one", "two", "three")),
            Arguments.of("c \"one two\" three", listOf("one two", "three")),
        )
    }
}
