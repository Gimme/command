package com.github.gimme.gimmebot.core.command

import com.github.gimme.gimmebot.core.command.executor.CommandExecutor
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.MethodSource

class BaseCommandTest {

    @ParameterizedTest
    @CsvSource(
        "test, test",
        "test, TEST",
        "test, tESt",
    )
    fun `command names should become lower case`(expectedName: String, inputName: String) {
        val command = object : BaseCommand(inputName) {}
        assertEquals(expectedName, command.name)
    }

    @Test
    fun `should execute reflection command with all types`() {
        var called = false

        val command = object : BaseCommand("c") {
            @CommandExecutor
            fun c(
                string1: String,
                string2: String,
                int1: Int,
                int2: Int,
                double1: Double,
                double2: Double,
                boolean1: Boolean,
                boolean2: Boolean?,
                boolean3: Boolean?,
            ) {
                assertEquals("string", string1)
                assertEquals("", string2)
                assertEquals(1, int1)
                assertEquals(-999, int2)
                assertEquals(0.5, double1)
                assertEquals(36.0, double2)
                assertEquals(true, boolean1)
                assertEquals(false, boolean2)
                Assertions.assertNull(boolean3)

                called = true
            }
        }

        command.execute(DUMMY_CONSOLE_COMMAND_SENDER,
            listOf("string", "", "1", "-999", "0.5", "36", "trUE", "false", "aaa"))

        assertTrue(called)
    }

    @ParameterizedTest
    @MethodSource("commandExecutor")
    fun `should execute reflection command`(
        input: String?,
        command: Command,
        shouldExecute: Boolean,
    ) {
        val expected = DUMMY_RESPONSE
        val actual = command.execute(DUMMY_CONSOLE_COMMAND_SENDER, input?.split(" ") ?: listOf())

        if (shouldExecute) assertEquals(expected, actual, "Command was not executed when it should have been")
        else Assertions.assertNull(actual, "Command was executed when it shouldn't have been")
    }

    @Test
    fun `should execute reflection command passing command sender subtypes`() {
        val command: Command = object : BaseCommand("c") {
            @CommandExecutor
            fun c(sender: CommandSenderImpl): CommandResponse? {
                assertEquals(1, sender.getInt())
                return DUMMY_RESPONSE
            }
        }
        val commandSender: CommandSender = CommandSenderImpl()

        val expected = DUMMY_RESPONSE
        val actual = command.execute(commandSender, listOf())

        assertEquals(expected, actual, "Command was not executed when it should have been")
    }

    companion object {
        @JvmStatic
        fun commandExecutor() = listOf(
            // BASIC TESTS
            Arguments.of(
                null,
                object : BaseCommand("c") {
                    @CommandExecutor
                    fun c(): CommandResponse? = DUMMY_RESPONSE
                },
                true),

            // VARARG TESTS
            Arguments.of(
                null,
                object : BaseCommand("c") {
                    @CommandExecutor
                    fun c(vararg strings: String): CommandResponse? {
                        Assertions.assertIterableEquals(listOf<String>(), strings.asIterable())
                        return DUMMY_RESPONSE
                    }
                },
                true),
            Arguments.of(
                "string1 string2 string3",
                object : BaseCommand("c") {
                    @CommandExecutor
                    fun c(vararg strings: String): CommandResponse? {
                        Assertions.assertIterableEquals(listOf("string1", "string2", "string3"), strings.asIterable())
                        return DUMMY_RESPONSE
                    }
                },
                true),
            Arguments.of(
                "a 1 2 3",
                object : BaseCommand("c") {
                    @CommandExecutor
                    fun c(string: String, vararg ints: Int): CommandResponse {
                        assertEquals("a", string)
                        Assertions.assertIterableEquals(listOf(1, 2, 3), ints.asIterable())
                        return DUMMY_RESPONSE
                    }
                },
                true),
            Arguments.of(
                "1",
                object : BaseCommand("c") {
                    @CommandExecutor
                    fun c(vararg a: Double): CommandResponse = DUMMY_RESPONSE
                },
                true),
            Arguments.of(
                "true",
                object : BaseCommand("c") {
                    @CommandExecutor
                    fun c(vararg a: Boolean): CommandResponse = DUMMY_RESPONSE
                },
                true),

            Arguments.of(
                null,
                object : BaseCommand("c") {
                    @CommandExecutor
                    fun c(a: String): CommandResponse = DUMMY_RESPONSE
                },
                false),
            Arguments.of(
                "a b",
                object : BaseCommand("c") {
                    @CommandExecutor
                    fun c(a: String): CommandResponse = DUMMY_RESPONSE
                },
                false),
            Arguments.of(
                "a",
                object : BaseCommand("c") {
                    @CommandExecutor
                    fun c(a: Int): CommandResponse = DUMMY_RESPONSE
                },
                false),
            Arguments.of(
                "1.0",
                object : BaseCommand("c") {
                    @CommandExecutor
                    fun c(vararg a: Int): CommandResponse = DUMMY_RESPONSE
                },
                false),
            Arguments.of(
                "a",
                object : BaseCommand("c") {
                    @CommandExecutor
                    fun c(a: Boolean): CommandResponse = DUMMY_RESPONSE
                },
                false),

            // DEFAULTS TESTS
            Arguments.of(
                "a",
                object : BaseCommand("c") {
                    @CommandExecutor
                    fun c(a: String = "def"): CommandResponse {
                        assertEquals("a", a)
                        return DUMMY_RESPONSE
                    }
                },
                true),
            Arguments.of(
                "a",
                object : BaseCommand("c") {
                    @CommandExecutor
                    fun c(a: String, b: String = "def"): CommandResponse {
                        assertEquals("def", b)
                        return DUMMY_RESPONSE
                    }
                },
                true),
            Arguments.of(
                "1",
                object : BaseCommand("c") {
                    @CommandExecutor
                    fun c(a: Int = 0, b: Int = 3, c: Int = 44): CommandResponse {
                        assertEquals(1, a)
                        assertEquals(3, b)
                        assertEquals(44, c)
                        return DUMMY_RESPONSE
                    }
                },
                true),

            // COMMAND SENDER TESTS
            Arguments.of(
                null,
                object : BaseCommand("c") {
                    @CommandExecutor
                    fun c(sender: CommandSender): CommandResponse {
                        assertEquals(DUMMY_CONSOLE_COMMAND_SENDER, sender)
                        return DUMMY_RESPONSE
                    }
                },
                true),
            Arguments.of(
                "1",
                object : BaseCommand("c") {
                    @CommandExecutor
                    fun c(sender: CommandSender, a: Int = 0): CommandResponse {
                        assertEquals(DUMMY_CONSOLE_COMMAND_SENDER, sender)
                        assertEquals(1, a)
                        return DUMMY_RESPONSE
                    }
                },
                true),
        )
    }

    private class CommandSenderImpl() : CommandSender {
        override val medium: CommandSender.Medium
            get() = CommandSender.Medium.CONSOLE

        override fun sendMessage(message: String) {
        }

        fun getInt(): Int {
            return 1
        }
    }
}
