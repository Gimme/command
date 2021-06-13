package dev.gimme.gimmeapi.command.property

import dev.gimme.gimmeapi.command.DUMMY_COMMAND_SENDER
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll

internal class DelegatePropertyTest {

    @Test
    fun `Converts delegate properties to parameters that take arguments`() {
        val listInput = listOf("a", "b")

        var called = false

        val command = object : PropertyCommand<Unit>("test-command") {

            val string: String by param()
            val int: Int by param()
            val double: Double by param()
            val boolean: Boolean by param()
            val list: List<String> by param()
            val collection: Collection<String> by param()
            val iterable: Iterable<String> by param()

            override fun call() {
                called = true
                assertAll(
                    { assertEquals("a", string) },
                    { assertEquals(1, int) },
                    { assertEquals(0.5, double) },
                    { assertEquals(true, boolean) },
                    { assertEquals(listInput, list) },
                    { assertEquals(listInput, collection) },
                    { assertEquals(listInput, iterable) },
                )
            }
        }
        val input = mapOf<String, Any?>(
            "string" to "a",
            "int" to 1,
            "double" to 0.5,
            "boolean" to true,
        )
            .map { Pair(command.parameters[it.key]!!, it.value) }
            .plus( Pair(command.parameters["list"]!!, listInput))
            .plus( Pair(command.parameters["collection"]!!, listInput))
            .plus( Pair(command.parameters["iterable"]!!, listInput))
            .toMap()

        command.execute(DUMMY_COMMAND_SENDER, input)
        assertTrue(called)
    }
}
