package dev.gimme.command.property

import dev.gimme.command.BaseCommand
import dev.gimme.command.DUMMY_COMMAND_SENDER
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class PropertyCommandTest {

    private val sender = DUMMY_COMMAND_SENDER

    @Test
    fun test() {
        var called = false

        val args = listOf(
            "abc",
            123,
            listOf(1.3, 0.0),
            setOf("x"),
        )

        val command = object : BaseCommand<Unit>("k") {

            val a: String? by param()

            val b: Int? by param()

            val list: List<Double> by param()

            val set: Set<String> by param()

            override fun call() {
                called = true

                assertAll(
                    { assertEquals(sender, sender) },
                    { assertEquals(args[0], a) },
                    { assertEquals(args[1], b) },
                    { assertEquals(args[2], list) },
                    { assertEquals(args[3], set) },
                )
            }
        }

        assertFalse(called)
        command.execute(sender, args.mapIndexed { index, arg -> command.parameters.getAt(index) to arg }.toMap())
        assertTrue(called)

        assertNotNull(command.parameters["a"])
        assertNotNull(command.parameters["b"])
        assertNotNull(command.parameters["list"])
        assertNotNull(command.parameters["set"])
    }
}
