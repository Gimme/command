package com.github.gimme.gimmebot.core.command

import com.github.gimme.gimmebot.core.command.executor.CommandExecutor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertIterableEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
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
            ) {
                assertAll(
                    { assertEquals("string", string1) },
                    { assertEquals("", string2) },
                    { assertEquals(1, int1) },
                    { assertEquals(-999, int2) },
                    { assertEquals(0.5, double1) },
                    { assertEquals(36.0, double2) },
                    { assertEquals(true, boolean1) },
                    { assertEquals(false, boolean2) },
                )

                called = true
            }
        }

        assertFalse(called)

        command.execute(DUMMY_COMMAND_SENDER,
            listOf("string", "", "1", "-999", "0.5", "36", "trUE", "false"))

        assertTrue(called)
    }

    @ParameterizedTest
    @MethodSource("commandExecutor")
    fun `should execute reflection command`(
        args: String?,
        command: Command,
        shouldExecute: Boolean = true,
    ) {
        val expected = DUMMY_RESPONSE
        val actual = command.execute(DUMMY_COMMAND_SENDER, args?.split(" ") ?: listOf())

        if (shouldExecute) assertEquals(expected, actual, "Command was not executed when it should have been")
        else assertEquals(CommandResponse.Status.ERROR,
            actual!!.status,
            "Command did not return an error message when it should have")
    }

    @Test
    fun `should execute reflection command when using sender subtypes`() {
        val command: Command = object : BaseCommand("c") {
            @CommandExecutor
            fun c(sender: CommandSenderImpl): CommandResponse {
                assertEquals(1, sender.getInt())
                return DUMMY_RESPONSE
            }
        }
        val commandSender: CommandSender = CommandSenderImpl()

        val expected = DUMMY_RESPONSE
        val actual = command.execute(commandSender, listOf())

        assertEquals(expected, actual, "Command was not executed when it should have been")
    }

    @ParameterizedTest
    @MethodSource("commandError")
    fun `should return command error`(
        args: String?,
        response: CommandResponse,
        sender: CommandSender,
    ) {
        val command: Command = object : BaseCommand("c") {
            @CommandExecutor
            fun c(sender: CommandSenderImpl, a: Int, b: Int? = null): CommandResponse {
                assertEquals(1, sender.getInt())
                return DUMMY_RESPONSE
            }
        }

        assertEquals(response, command.execute(sender, args?.split(" ") ?: listOf()))
    }

    @Test
    fun `camel case should be split into separate lowercase words`() {
        assertAll(
            { assertEquals("lorem ipsum", "loremIpsum".splitCamelCase(" ")) },
            { assertEquals("lorem ipsum", "LoremIpsum".splitCamelCase(" ")) },
            { assertEquals("lorem ipsum", "lorem ipsum".splitCamelCase(" ")) },
            { assertEquals("lorem ipsum", "Lorem Ipsum".splitCamelCase(" ")) },
            { assertEquals("lorem-ipsum", "loremIpsum".splitCamelCase("-")) },
            { assertEquals("lorem-ipsum", "lorem ipsum".splitCamelCase("-")) },
        )
    }

    @Test
    fun `should get command usage`() {
        val command = object : BaseCommand("c") {
            @CommandExecutor("", "2")
            fun a(paramOne: Int, paramTwo: Int = 2) {
            }
        }

        assertEquals("c <param-one> <param-two=2>", command.usage)
    }

    companion object {
        @JvmStatic
        fun commandExecutor() = listOf(
            // BASIC TESTS
            Arguments.of(
                null,
                object : BaseCommand("c") {
                    @CommandExecutor
                    fun c(): CommandResponse = DUMMY_RESPONSE
                },
                true,
            ),

            // VARARG TESTS
            Arguments.of(
                null,
                object : BaseCommand("c") {
                    @CommandExecutor
                    fun c(vararg strings: String): CommandResponse {
                        assertIterableEquals(listOf<String>(), strings.asIterable())
                        return DUMMY_RESPONSE
                    }
                },
                true,
            ),
            Arguments.of(
                "string1 string2 string3",
                object : BaseCommand("c") {
                    @CommandExecutor
                    fun c(vararg strings: String): CommandResponse {
                        assertIterableEquals(listOf("string1", "string2", "string3"), strings.asIterable())
                        return DUMMY_RESPONSE
                    }
                },
                true,
            ),
            Arguments.of(
                "a 1 2 3",
                object : BaseCommand("c") {
                    @CommandExecutor
                    fun c(string: String, vararg ints: Int): CommandResponse {
                        assertEquals("a", string)
                        assertIterableEquals(listOf(1, 2, 3), ints.asIterable())
                        return DUMMY_RESPONSE
                    }
                },
                true,
            ),
            Arguments.of(
                "1",
                object : BaseCommand("c") {
                    @CommandExecutor
                    fun c(vararg a: Double): CommandResponse = DUMMY_RESPONSE
                },
                true,
            ),
            Arguments.of(
                "true",
                object : BaseCommand("c") {
                    @CommandExecutor
                    fun c(vararg a: Boolean): CommandResponse = DUMMY_RESPONSE
                },
                true,
            ),

            Arguments.of(
                null,
                object : BaseCommand("c") {
                    @CommandExecutor
                    fun c(a: String): CommandResponse = DUMMY_RESPONSE
                },
                false,
            ),
            Arguments.of(
                "a b",
                object : BaseCommand("c") {
                    @CommandExecutor
                    fun c(a: String): CommandResponse = DUMMY_RESPONSE
                },
                false,
            ),
            Arguments.of(
                "a",
                object : BaseCommand("c") {
                    @CommandExecutor
                    fun c(a: Int): CommandResponse = DUMMY_RESPONSE
                },
                false,
            ),
            Arguments.of(
                "1.0",
                object : BaseCommand("c") {
                    @CommandExecutor
                    fun c(vararg a: Int): CommandResponse = DUMMY_RESPONSE
                },
                false,
            ),
            Arguments.of(
                "a",
                object : BaseCommand("c") {
                    @CommandExecutor
                    fun c(a: Boolean): CommandResponse = DUMMY_RESPONSE
                },
                false,
            ),

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
                true,
            ),
            Arguments.of(
                "a",
                object : BaseCommand("c") {
                    @CommandExecutor
                    fun c(a: String, b: String = "def"): CommandResponse {
                        assertEquals("a", a)
                        assertEquals("def", b)
                        return DUMMY_RESPONSE
                    }
                },
                true,
            ),
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
                true,
            ),
            Arguments.of(
                null,
                object : BaseCommand("c") {
                    @CommandExecutor
                    fun c(a: Int? = 4): CommandResponse {
                        assertEquals(4, a)
                        return DUMMY_RESPONSE
                    }
                },
                true,
            ),
            Arguments.of(
                null,
                object : BaseCommand("c") {
                    @CommandExecutor
                    fun c(a: Int? = null): CommandResponse {
                        assertEquals(null, a)
                        return DUMMY_RESPONSE
                    }
                },
                true,
            ),
            Arguments.of(
                "abc",
                object : BaseCommand("c") {
                    @CommandExecutor
                    fun c(a: Int? = null): CommandResponse = DUMMY_RESPONSE
                },
                false,
            ),

            // COMMAND SENDER TESTS
            Arguments.of(
                null,
                object : BaseCommand("c") {
                    @CommandExecutor
                    fun c(sender: CommandSender): CommandResponse {
                        assertEquals(DUMMY_COMMAND_SENDER, sender)
                        return DUMMY_RESPONSE
                    }
                },
                true,
            ),
            Arguments.of(
                "1",
                object : BaseCommand("c") {
                    @CommandExecutor
                    fun c(sender: CommandSender, a: Int = 0): CommandResponse {
                        assertEquals(DUMMY_COMMAND_SENDER, sender)
                        assertEquals(1, a)
                        return DUMMY_RESPONSE
                    }
                },
                true,
            ),
        )

        @JvmStatic
        fun commandError() = listOf(
            Arguments.of(
                "1",
                DUMMY_RESPONSE,
                CommandSenderImpl(),
            ),
            Arguments.of(
                "a",
                INVALID_ARGUMENT_ERROR,
                CommandSenderImpl(),
            ),
            Arguments.of(
                "1 a",
                INVALID_ARGUMENT_ERROR,
                CommandSenderImpl(),
            ),
            Arguments.of(
                "1",
                INCOMPATIBLE_SENDER_ERROR,
                DUMMY_COMMAND_SENDER,
            ),
            Arguments.of(
                null,
                TOO_FEW_ARGUMENTS_ERROR,
                CommandSenderImpl(),
            ),
            Arguments.of(
                "1 2 3",
                TOO_MANY_ARGUMENTS_ERROR,
                CommandSenderImpl(),
            ),
        )
    }

    private class CommandSenderImpl : CommandSender {
        override val name: String
            get() = ""

        override fun sendMessage(message: String) {}

        fun getInt(): Int {
            return 1
        }
    }
}
