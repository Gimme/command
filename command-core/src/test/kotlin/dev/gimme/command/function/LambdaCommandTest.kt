package dev.gimme.command.function

import dev.gimme.command.DUMMY_COMMAND_SENDER
import dev.gimme.command.channel.TextCommandChannel
import dev.gimme.command.sender.CommandSender
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class LambdaCommandTest {

    private val sender = DUMMY_COMMAND_SENDER

    private val channel = object : TextCommandChannel() {
        override fun onEnable() {
        }

        override fun onDisable() {
        }
    }

    @Test
    fun test() {
        var called = false

        val commandName = "c"

        val command = LambdaCommand(commandName) { sender: CommandSender ->
            assertEquals(this.sender, sender)
            called = true
        }

        assertFalse(called)

        channel.commandManager.registerCommand(command)
        channel.parseInput(sender, commandName)

        assertTrue(called)
    }
}
