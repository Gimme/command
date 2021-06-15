package dev.gimme.gimmeapi.command.property

import dev.gimme.gimmeapi.command.DUMMY_COMMAND_SENDER
import dev.gimme.gimmeapi.command.channel.TextCommandChannel
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
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
        val args = listOf(
            "abc",
            123,
            1.3,
            "x",
        )

        val command = object : PropertyCommand<Unit>(commandName) {

            val a: String? by param()

            val b: Param<Int?> = param(Int::class)
                .name("bb")
                .build()

            val list: List<Double> by param<List<Double>>().name("c")

            val set: Set<String> by param()

            override fun call() {
                called = true

                assertAll(
                    { assertEquals(sender, sender) },
                    { assertEquals(args[0], a) },
                    { assertEquals(args[1], b.get()) },
                    { assertEquals(listOf(args[2]), list) },
                    { assertEquals(setOf(args[3]), set) },
                )
            }
        }

        assertFalse(called)

        channel.commandManager.registerCommand(command)
        channel.parseInput(sender, "$commandName ${args.joinToString(" ")}")

        assertTrue(called)

        assertNotNull(command.parameters["a"])
        assertNotNull(command.parameters["bb"])
        assertNotNull(command.parameters["c"])
        assertNotNull(command.parameters["set"])
    }
}
