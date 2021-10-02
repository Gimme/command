package dev.gimme.command.channel.manager

import dev.gimme.command.Command
import dev.gimme.command.channel.DUMMY_COMMAND
import dev.gimme.command.channel.DUMMY_COMMAND_SENDER
import dev.gimme.command.channel.DefaultBaseCommand
import dev.gimme.command.parameter.CommandParameter
import dev.gimme.command.sender.CommandSender
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertIterableEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class SimpleCommandManagerTest {

    private val commandManager: CommandManager<Any?> = SimpleCommandManager { it }

    @Test
    fun `should register command`() {
        commandManager.registerCommand(DUMMY_COMMAND)

        assertAll(
            { assertNull(commandManager.getCommand(listOf("test726"))) },
            { assertEquals(DUMMY_COMMAND, commandManager.getCommand(listOf("test"))) },
        )
    }

    @Test
    fun `register command with same name should overwrite`() {
        commandManager.registerCommand(object : DefaultBaseCommand("test") {})
        commandManager.registerCommand(DUMMY_COMMAND)

        assertEquals(DUMMY_COMMAND, commandManager.getCommand(listOf("test")))
    }

    @ParameterizedTest
    @CsvSource(
        "test",
        "TEST",
        "Test",
    )
    fun `should execute command`(commandName: String) {
        val command = mockk<Command<Any>>(relaxed = true)
        val args = mapOf<CommandParameter, Any?>()

        every { command.name } returns commandName

        commandManager.registerCommand(command)
        commandManager.executeCommand(DUMMY_COMMAND_SENDER, command, args)

        verify(exactly = 1) { command.execute(DUMMY_COMMAND_SENDER, args) }
    }

    @ParameterizedTest
    @CsvSource(
        "a, b",
        "te/st, .",
    )
    fun `Executes hierarchical command`(commandParent: String, commandName: String) {
        var executed = false

        val command = object : DefaultBaseCommand(commandName, DefaultBaseCommand(commandParent)) {
            override fun execute(commandSender: CommandSender, args: Map<CommandParameter, Any?>) {
                executed = true
            }
        }

        commandManager.registerCommand(command)
        commandManager.executeCommand(DUMMY_COMMAND_SENDER, command, mapOf())

        assertTrue(executed)
    }

    @Test
    fun `should execute parent child commands separately`() {
        var parentExecuted = false
        var childExecuted = false

        val parentCommand = object : DefaultBaseCommand("parent") {
            override fun execute(commandSender: CommandSender, args: Map<CommandParameter, Any?>) {
                assertIterableEquals(listOf("a", "b"), args.values)
                parentExecuted = true
            }
        }
        val childCommand = object : DefaultBaseCommand("child", DefaultBaseCommand("parent")) {
            override fun execute(commandSender: CommandSender, args: Map<CommandParameter, Any?>) {
                assertIterableEquals(listOf("x", "y"), args.values)
                childExecuted = true
            }
        }

        commandManager.registerCommand(parentCommand)
        commandManager.registerCommand(childCommand)
        commandManager.executeCommand(
            DUMMY_COMMAND_SENDER, parentCommand, mapOf(
                mockk<CommandParameter>() to "a",
                mockk<CommandParameter>() to "b"
            )
        )
        commandManager.executeCommand(
            DUMMY_COMMAND_SENDER, childCommand, mapOf(
                mockk<CommandParameter>() to "x",
                mockk<CommandParameter>() to "y"
            )
        )

        assertTrue(parentExecuted)
        assertTrue(childExecuted)

        parentExecuted = false
        childExecuted = false

        val commandManager2 = SimpleCommandManager { it }
        commandManager2.registerCommand(childCommand)
        commandManager2.registerCommand(parentCommand)
        commandManager2.executeCommand(
            DUMMY_COMMAND_SENDER, childCommand, mapOf(
                mockk<CommandParameter>() to "x",
                mockk<CommandParameter>() to "y"
            )
        )
        commandManager2.executeCommand(
            DUMMY_COMMAND_SENDER, parentCommand, mapOf(
                mockk<CommandParameter>() to "a",
                mockk<CommandParameter>() to "b"
            )
        )

        assertTrue(childExecuted)
        assertTrue(parentExecuted)
    }
}
