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
    private val consoleCommandSender = DummyCommandSender(CommandSender.Medium.CONSOLE)
    private val chatCommandSender = DummyCommandSender(CommandSender.Medium.CHAT)

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
        commandManager.parseInput(consoleCommandSender, inputCommand)

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
        commandManager.parseInput(consoleCommandSender, input)

        assertNotNull(actualArgs)
        assertIterableEquals(expectedArgs, actualArgs)
    }

    @ParameterizedTest
    @MethodSource("commandSenderMedium")
    fun `should accept command if correct prefix`(medium: CommandSender.Medium, input: String, shouldExecute: Boolean) {
        commandManager.registerCommand(testCommand)
        val executed: Boolean = commandManager.parseInput(object : CommandSender {
            override val medium: CommandSender.Medium
                get() = medium

            override fun sendMessage(message: String) {}
        }, input)

        assertEquals(shouldExecute, executed)
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

        @JvmStatic
        fun commandSenderMedium() = listOf(
            Arguments.of(CommandSender.Medium.CONSOLE, "test", true),
            Arguments.of(CommandSender.Medium.CONSOLE, "!test", false),
            Arguments.of(CommandSender.Medium.CHAT, "test", false),
            Arguments.of(CommandSender.Medium.CHAT, "!test", true),
        )
    }

    private class DummyCommandSender(override val medium: CommandSender.Medium) : CommandSender {
        override fun sendMessage(message: String) {}
    }
}
