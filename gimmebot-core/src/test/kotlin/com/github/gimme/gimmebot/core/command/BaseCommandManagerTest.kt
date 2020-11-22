package com.github.gimme.gimmebot.core.command

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.function.Executable
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.MethodSource

class BaseCommandManagerTest {

    private val prefix = "!"
    private val commandManager: CommandManager = object : BaseCommandManager(prefix) {}
    private val testCommand: Command = object : BaseCommand("test") {
        override fun execute(commandSender: CommandSender, args: List<String>): CommandResponse? {
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

    @ParameterizedTest
    @CsvSource(
        "test, test",
        "Test, test",
        "test, TEST",
        "Test, Test",
    )
    fun `should execute command`(commandName: String, inputCommand: String) {
        var executed = false

        val command: Command = object : BaseCommand(commandName) {
            override fun execute(commandSender: CommandSender, args: List<String>): CommandResponse? {
                executed = true
                return null
            }
        }

        commandManager.registerCommand(command)
        commandManager.parseInput(dummyCommandSender, prefix + inputCommand)

        assertTrue(executed)
    }

    @ParameterizedTest
    @MethodSource("args")
    fun `should pass arguments`(input: String, expectedArgs: List<String>) {
        var actualArgs: List<String>? = null

        val command: Command = object : BaseCommand("c") {
            override fun execute(commandSender: CommandSender, args: List<String>): CommandResponse? {
                actualArgs = args
                return null
            }
        }

        commandManager.registerCommand(command)
        commandManager.parseInput(dummyCommandSender, prefix + input)

        assertNotNull(actualArgs)
        assertIterableEquals(expectedArgs, actualArgs)
    }

    companion object {
        @JvmStatic
        fun args() = listOf(
            Arguments.of("c", emptyList<String>()),
            Arguments.of("c ", emptyList<String>()),
            Arguments.of("c one", listOf("one")),
            Arguments.of("c one two", listOf("one", "two")),
            Arguments.of("c one two three", listOf("one", "two", "three")),
        )
    }
}
