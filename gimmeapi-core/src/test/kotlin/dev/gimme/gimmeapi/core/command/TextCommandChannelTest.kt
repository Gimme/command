package dev.gimme.gimmeapi.core.command

import dev.gimme.gimmeapi.core.command.channel.TextCommandChannel
import dev.gimme.gimmeapi.core.command.manager.TextCommandManager
import dev.gimme.gimmeapi.core.command.parameter.CommandParameter
import dev.gimme.gimmeapi.core.command.sender.CommandSender
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import kotlin.reflect.full.createType

class TextCommandChannelTest {

    @ParameterizedTest
    @MethodSource("args")
    fun `should pass arguments`(input: String, expectedArgs: List<String>) {
        val commandManager = TextCommandManager()

        var actualArgs: Collection<Any?>? = null

        val command = object : DefaultBaseCommand("c") {
            init {
                parameters.addAll(expectedArgs.mapIndexed { index, _ ->
                    CommandParameter(index.toString(), "", String::class.createType())
                })
            }

            override fun execute(commandSender: CommandSender, args: Map<CommandParameter, Any?>) {
                actualArgs = args.values
            }
        }

        commandManager.registerCommand(command)

        object : TextCommandChannel(includeConsoleListener = false) {
            override fun onEnable() {
                parseInput(DUMMY_COMMAND_SENDER, input)
            }

            override fun onDisable() {}
        }.apply {
            registerCommandManager(commandManager)
            enable()
        }

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
