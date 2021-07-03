package dev.gimme.command

import dev.gimme.command.channel.TextCommandChannel
import dev.gimme.command.manager.TextCommandManager
import dev.gimme.command.parameter.CommandParameter
import dev.gimme.command.parameter.ParameterTypes
import dev.gimme.command.sender.CommandSender
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

class TextCommandChannelTest {

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
