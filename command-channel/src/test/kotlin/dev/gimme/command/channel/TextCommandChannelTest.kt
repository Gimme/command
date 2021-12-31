package dev.gimme.command.channel

import dev.gimme.command.BaseCommand
import dev.gimme.command.annotations.Default
import dev.gimme.command.annotations.Parameter
import dev.gimme.command.channel.manager.TextCommandManager
import dev.gimme.command.annotations.CommandFunction
import dev.gimme.command.parameter.CommandParameter
import dev.gimme.command.parameter.ParameterTypes
import dev.gimme.command.sender.CommandSender
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertIterableEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

class TextCommandChannelTest {

    private val sender = DUMMY_COMMAND_SENDER
    private val channel = object : TextCommandChannel(includeConsoleListener = false) {
        override fun onEnable() {
        }

        override fun onDisable() {
        }
    }

    @ParameterizedTest
    @MethodSource("args")
    fun `should pass arguments`(input: String, expectedArgs: List<String>) {
        val commandManager = TextCommandManager()

        var actualArgs: Collection<Any?>? = null

        val command = object : DefaultBaseCommand("c") {
            init {
                parameters.addAll(expectedArgs.mapIndexed { index, _ ->
                    CommandParameter(index.toString(), "", ParameterTypes.get(String::class))
                })
            }

            override fun execute(commandSender: CommandSender, args: Map<CommandParameter, Any?>) {
                actualArgs = args.values
            }
        }

        commandManager.registerCommand(command)

        channel.registerCommandManager(commandManager)
        channel.parseInput(sender, input)

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

    @Test
    fun `Calls function command with parameters`() {
        var called = false

        val commandName = "k"
        val arg1 = "abc"
        val arg2 = 123
        val arg3 = "three"
        val arg4 = "four"

        val command = object : BaseCommand<Any?>(commandName) {

            @CommandFunction
            private fun call(s: CommandSender, a: String, b: Int, c: List<String>) {
                called = true

                assertEquals(s, sender)
                assertEquals(arg1, a)
                assertEquals(arg2, b)
                assertEquals(listOf(arg3, arg4), c)
            }
        }

        assertFalse(called)

        channel.commandManager.registerCommand(command)
        channel.parseInput(sender, "$commandName $arg1 $arg2 $arg3 $arg4")

        assertTrue(called)

        assertNotNull(command.parameters["a"])
        assertNotNull(command.parameters["b"])
        assertNotNull(command.parameters["c"])
    }

    @Test
    fun `Uses default values`() {
        var called = false

        val commandName = "k"
        val arg1 = "abc"

        val command = object : BaseCommand<Any?>(commandName) {

            @CommandFunction
            private fun call(
                @Parameter(def = Default("xyz"))
                a: String,
                @Parameter(def = Default("xyz"))
                b: String,
                @Parameter(def = Default("5"))
                c: Int,
            ) {
                called = true

                assertEquals(arg1, a)
                assertEquals("xyz", b)
                assertEquals(5, c)
            }
        }

        assertFalse(called)

        channel.commandManager.registerCommand(command)
        channel.parseInput(sender, "$commandName $arg1")

        assertTrue(called)
    }

    @Test
    fun `Handles named argument`() {
        var called = 0

        val commandName = "k"
        val arg1 = "aa"

        val command = object : BaseCommand<Unit>(commandName) {
            val abc: String by param()

            override fun call() {
                assertEquals(arg1, abc)
                called++
            }
        }

        assertEquals(0, called)

        channel.commandManager.registerCommand(command)
        channel.parseInput(sender, "$commandName --abc $arg1")
        channel.parseInput(sender, "$commandName -a $arg1")

        assertEquals(2, called)
    }

    @Test
    fun `Handles multiple named arguments`() {
        var called = 0

        val commandName = "k"
        val arg1 = "aa"
        val arg2 = 1
        val arg3 = true

        val command = object : BaseCommand<Unit>(commandName) {
            val abc: String by param()
            val def: Int by param()
            val ghi: Boolean by param()

            override fun call() {
                called++
            }
        }

        assertEquals(0, called)

        channel.commandManager.registerCommand(command)
        channel.parseInput(sender, "$commandName --abc $arg1 --ghi $arg3 --def $arg2")
        channel.parseInput(sender, "$commandName -a $arg1 -g $arg3 $arg2")
        channel.parseInput(sender, "$commandName -g $arg3 $arg1 $arg2")

        assertEquals(3, called)
    }

    @Test
    fun `Handles boolean named arguments`() {
        var called = 0

        val commandName = "k"

        val command = object : BaseCommand<Unit>(commandName) {
            val abc: Boolean by param<Boolean>().default(false)
            val def: Boolean by param<Boolean>().default(false)
            val ghi: Boolean by param<Boolean>().default(false)

            override fun call() {
                assertTrue(abc)
                assertFalse(def)
                assertTrue(ghi)
                called++
            }
        }

        assertEquals(0, called)

        channel.commandManager.registerCommand(command)
        channel.parseInput(sender, "$commandName --abc true -g")
        channel.parseInput(sender, "$commandName -agd false")

        assertEquals(2, called)
    }
}
