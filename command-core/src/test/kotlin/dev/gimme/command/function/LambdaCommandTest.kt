package dev.gimme.command.function

import dev.gimme.command.DUMMY_COMMAND_SENDER
import dev.gimme.command.sender.CommandSender
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class LambdaCommandTest {

    private val sender = DUMMY_COMMAND_SENDER

    @Test
    fun test() {
        var called = false

        val command = LambdaCommand("c") { sender: CommandSender ->
            assertEquals(this.sender, sender)
            called = true
        }

        assertFalse(called)
        command.execute(sender, mapOf())
        assertTrue(called)
    }
}
