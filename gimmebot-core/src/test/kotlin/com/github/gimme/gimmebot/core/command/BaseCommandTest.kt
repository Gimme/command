package com.github.gimme.gimmebot.core.command

import com.github.gimme.gimmebot.core.command.executor.CommandExecutor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertIterableEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
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
        val command = object : BaseCommand<Unit>(inputName) {}
        assertEquals(expectedName, command.name)
    }

    @Test
    fun `should execute reflection command with all types`() {
        var called = false

        val command = object : BaseCommand<Unit>("c") {
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
        command.execute(DUMMY_COMMAND_SENDER, listOf("string", "", "1", "-999", "0.5", "36", "trUE", "false"))
        assertTrue(called)
    }

    @Test
    fun `command without return type should execute`() {
        var called = false

        val command = object : BaseCommand<Unit>("c") {
            @CommandExecutor
            fun c(string1: String) {
                called = true
            }
        }

        assertFalse(called)
        command.execute(DUMMY_COMMAND_SENDER, listOf("abc"))
        assertTrue(called)
    }

    @ParameterizedTest
    @MethodSource("commandExecutor")
    fun `should execute reflection command`(
        args: String?,
        command: Command<String>,
        shouldExecute: Boolean = true,
    ) {
        val expected = DUMMY_RESPONSE

        try {
            val actual = command.execute(DUMMY_COMMAND_SENDER, args?.split(" ") ?: listOf())

            assertEquals(expected, actual, "Command was not executed when it should have been")
        } catch (e: CommandException) {
            assertTrue(!shouldExecute, "Command did not return with an error when it should have")
        }
    }

    @Test
    fun `should execute reflection command when using sender subtypes`() {
        val command = object : BaseCommand<String>("c") {
            @CommandExecutor
            fun c(sender: CommandSenderImpl): String {
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
    fun `should throw command exception`(
        args: String?,
        commandException: CommandException?,
        sender: CommandSender,
    ) {
        val command = object : BaseCommand<String>("c") {
            @CommandExecutor
            fun c(sender: CommandSenderImpl, a: Int, b: Int? = null): String {
                assertEquals(1, sender.getInt())
                return DUMMY_RESPONSE
            }
        }

        val executeCommand = { command.execute(sender, args?.split(" ") ?: listOf()) }

        if (commandException == null) {
            assertDoesNotThrow { executeCommand() }
            return
        }

        val exception = assertThrows<CommandException> { executeCommand() }
        assertEquals(commandException, exception)
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
        val command = object : BaseCommand<Unit>("c") {
            @CommandExecutor("", "2")
            fun a(paramOne: Int, paramTwo: Int = 2) {
            }
        }

        assertEquals("c <param-one> <param-two=2>", command.usage)
    }

    @Test
    fun `should get command usage with command sender`() {
        val command = object : BaseCommand<Unit>("c") {
            @CommandExecutor("", "2")
            fun a(sender: CommandSender, paramOne: Int, paramTwo: Int = 2) {
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
                object : BaseCommand<String>("c") {
                    @CommandExecutor
                    fun c(): String = DUMMY_RESPONSE
                },
                true,
            ),

            // VARARG TESTS
            Arguments.of(
                null,
                object : BaseCommand<String>("c") {
                    @CommandExecutor
                    fun c(vararg strings: String): String {
                        assertIterableEquals(listOf<String>(), strings.asIterable())
                        return DUMMY_RESPONSE
                    }
                },
                true,
            ),
            Arguments.of(
                "string1 string2 string3",
                object : BaseCommand<String>("c") {
                    @CommandExecutor
                    fun c(vararg strings: String): String {
                        assertIterableEquals(listOf("string1", "string2", "string3"), strings.asIterable())
                        return DUMMY_RESPONSE
                    }
                },
                true,
            ),
            Arguments.of(
                "a 1 2 3",
                object : BaseCommand<String>("c") {
                    @CommandExecutor
                    fun c(string: String, vararg ints: Int): String {
                        assertEquals("a", string)
                        assertIterableEquals(listOf(1, 2, 3), ints.asIterable())
                        return DUMMY_RESPONSE
                    }
                },
                true,
            ),
            Arguments.of(
                "1",
                object : BaseCommand<String>("c") {
                    @CommandExecutor
                    fun c(vararg a: Double): String = DUMMY_RESPONSE
                },
                true,
            ),
            Arguments.of(
                "true",
                object : BaseCommand<String>("c") {
                    @CommandExecutor
                    fun c(vararg a: Boolean): String = DUMMY_RESPONSE
                },
                true,
            ),

            Arguments.of(
                null,
                object : BaseCommand<String>("c") {
                    @CommandExecutor
                    fun c(a: String): String = DUMMY_RESPONSE
                },
                false,
            ),
            Arguments.of(
                "a b",
                object : BaseCommand<String>("c") {
                    @CommandExecutor
                    fun c(a: String): String = DUMMY_RESPONSE
                },
                false,
            ),
            Arguments.of(
                "a",
                object : BaseCommand<String>("c") {
                    @CommandExecutor
                    fun c(a: Int): String = DUMMY_RESPONSE
                },
                false,
            ),
            Arguments.of(
                "1.0",
                object : BaseCommand<String>("c") {
                    @CommandExecutor
                    fun c(vararg a: Int): String = DUMMY_RESPONSE
                },
                false,
            ),
            Arguments.of(
                "a",
                object : BaseCommand<String>("c") {
                    @CommandExecutor
                    fun c(a: Boolean): String = DUMMY_RESPONSE
                },
                false,
            ),

            // DEFAULTS TESTS
            Arguments.of(
                "a",
                object : BaseCommand<String>("c") {
                    @CommandExecutor
                    fun c(a: String = "def"): String {
                        assertEquals("a", a)
                        return DUMMY_RESPONSE
                    }
                },
                true,
            ),
            Arguments.of(
                "a",
                object : BaseCommand<String>("c") {
                    @CommandExecutor
                    fun c(a: String, b: String = "def"): String {
                        assertEquals("a", a)
                        assertEquals("def", b)
                        return DUMMY_RESPONSE
                    }
                },
                true,
            ),
            Arguments.of(
                "1",
                object : BaseCommand<String>("c") {
                    @CommandExecutor
                    fun c(a: Int = 0, b: Int = 3, c: Int = 44): String {
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
                object : BaseCommand<String>("c") {
                    @CommandExecutor
                    fun c(a: Int? = 4): String {
                        assertEquals(4, a)
                        return DUMMY_RESPONSE
                    }
                },
                true,
            ),
            Arguments.of(
                null,
                object : BaseCommand<String>("c") {
                    @CommandExecutor
                    fun c(a: Int? = null): String {
                        assertEquals(null, a)
                        return DUMMY_RESPONSE
                    }
                },
                true,
            ),
            Arguments.of(
                "abc",
                object : BaseCommand<String>("c") {
                    @CommandExecutor
                    fun c(a: Int? = null): String = DUMMY_RESPONSE
                },
                false,
            ),

            // COMMAND SENDER TESTS
            Arguments.of(
                null,
                object : BaseCommand<String>("c") {
                    @CommandExecutor
                    fun c(sender: CommandSender): String {
                        assertEquals(DUMMY_COMMAND_SENDER, sender)
                        return DUMMY_RESPONSE
                    }
                },
                true,
            ),
            Arguments.of(
                "1",
                object : BaseCommand<String>("c") {
                    @CommandExecutor
                    fun c(sender: CommandSender, a: Int = 0): String {
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
                null,
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
