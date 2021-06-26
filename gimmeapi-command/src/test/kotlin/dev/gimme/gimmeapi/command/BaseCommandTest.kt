package dev.gimme.gimmeapi.command

import dev.gimme.gimmeapi.command.annotations.Parameter
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

    @Test
    fun parameterAnnotation() {
        val arg1 = "arg1"
        val command = object : BaseCommand<Any>("test-command") {
            @Parameter
            private lateinit var param1: String

            override fun call() {
                assertEquals(arg1, param1)

                called = true
            }
        }

        testCommand(
            command,
            mapOf(command.parameters.first() to arg1),
        )
    }

    @Test
    fun parameterDelegate() {
        val arg1 = "arg1"
        val command = object : BaseCommand<Any>("test-command") {
            private val param1: String by param()

            override fun call() {
                assertEquals(arg1, param1)

                called = true
            }
        }

        testCommand(
            command,
            mapOf(command.parameters.first() to arg1),
        )
    }

    private fun testCommand(
        command: BaseCommand<*>,
        args: Map<CommandParameter, Any?> = mapOf(),
        sender: CommandSender = dummySender,
    ) {
        assertFalse(called)
        command.execute(sender, args)
        assertTrue(called)
    }
}
