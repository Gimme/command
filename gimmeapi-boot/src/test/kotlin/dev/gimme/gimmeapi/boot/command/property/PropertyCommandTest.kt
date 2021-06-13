package dev.gimme.gimmeapi.boot.command.property

import dev.gimme.gimmeapi.boot.command.DUMMY_COMMAND_SENDER
import dev.gimme.gimmeapi.command.channel.TextCommandChannel
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class PropertyCommandTest {

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

        val commandName = "k"
        val arg1 = "abc"
        val arg2 = 123
        val arg3 = 1.3

        val command = object : PropertyCommand<Unit>(commandName) {

            val a: String by param()

            val b: Param<Int> = param<Int>()
                .setName("bb")
                .setType(Int::class)
                .build()

            val c: Double by param<Double>()
                .setName("ccc")

            override fun call() {
                called = true

                assertAll(
                    { assertEquals(sender, sender) },
                    { assertEquals(arg1, a) },
                    { assertEquals(arg2, b.getValue()) },
                    { assertEquals(arg3, c) },
                )
            }
        }

        assertFalse(called)

        channel.commandManager.registerCommand(command)
        channel.parseInput(sender, "$commandName $arg1 $arg2 $arg3")

        assertTrue(called)

        assertTrue(command.parameters["a"] != null)
        assertTrue(command.parameters["bb"] != null)
        assertTrue(command.parameters["ccc"] != null)
    }
}
