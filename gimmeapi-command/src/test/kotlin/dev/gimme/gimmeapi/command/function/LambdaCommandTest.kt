package dev.gimme.gimmeapi.command.function

import dev.gimme.gimmeapi.command.DUMMY_COMMAND_SENDER
import dev.gimme.gimmeapi.command.channel.TextCommandChannel
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

        val command = LambdaCommand(commandName) {
            called = true
        }

        assertFalse(called)

        channel.commandManager.registerCommand(command)
        channel.parseInput(sender, commandName)

        assertTrue(called)
    }
}
