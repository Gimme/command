package dev.gimme.gimmeapi.command

import dev.gimme.gimmeapi.command.annotations.Sender
import dev.gimme.gimmeapi.command.parameter.CommandParameter
import dev.gimme.gimmeapi.command.sender.CommandSender
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class BaseCommandTest {

    private var called = false
    private val dummySender = DUMMY_COMMAND_SENDER

    @Test
    fun senderAnnotation() {
        testCommand(object : BaseCommand<Any>("test-command") {
            @Sender
            private lateinit var sender: CommandSender

            override fun call() {
                assertEquals(dummySender, sender)

                called = true
            }
        })
    }

    @Test
    fun senderDelegate() {
        testCommand(object : BaseCommand<Any>("test-command") {
            private val sender: CommandSender by sender()

            override fun call() {
                assertEquals(dummySender, sender)

                called = true
            }
        })
    }

    private fun testCommand(
        command: BaseCommand<*>,
        sender: CommandSender = dummySender,
        args: Map<CommandParameter, Any?> = mapOf()
    ) {
        assertFalse(called)
        command.execute(sender, args)
        assertTrue(called)
    }
}
