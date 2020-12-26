package com.github.gimme.gimmebot.core.command

import com.github.gimme.gimmebot.core.command.manager.commandcollection.CommandTree
import com.github.gimme.gimmebot.core.command.medium.TextCommandMedium
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

class TextCommandMediumTest {

    @ParameterizedTest
    @MethodSource("args")
    fun `should pass arguments`(input: String, expectedArgs: List<String>) {
        val commands = CommandTree<String?>()

        var actualArgs: List<String>? = null

        val command = object : DefaultBaseCommand("c") {
            override fun execute(commandSender: CommandSender, args: List<String>) {
                actualArgs = args
            }
        }

        commands.addCommand(command)

        val commandInputMedium = object : TextCommandMedium(commands) {
            override val commandPrefix: String?
                get() = null

            override fun onInstall() {
                parseInput(DUMMY_COMMAND_SENDER, input)
            }
        }
        commandInputMedium.install()

        Assertions.assertNotNull(actualArgs)
        Assertions.assertIterableEquals(expectedArgs, actualArgs)
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
