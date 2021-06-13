package dev.gimme.gimmeapi.command

import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class ParameterTypesTest {

    private val parameterTypes: ParameterTypes = ParameterTypes

    @Test
    fun `Converts basic singular types`() {
        assertAll(
            { assertConverts("abc", "abc") },
            { assertConverts("1", 1) },
            { assertConverts("0.5", 0.5) },
        )
    }

    @Test
    fun `Converts generic types`() {
        data class A<T>(val a: T)

        parameterTypes.register { A(it.toDouble()) }

        assertConverts(
            "3",
            A(3.0),
        )
    }

    @Test
    fun `Registers custom type`() {
        data class A(val a: String)
        val value = "1"

        parameterTypes.register { A(it) }

        assertConverts(value, A(value))
    }

    @Test
    fun `Handles subtypes`() {
        abstract class A<T> {
            abstract val a: T
        }
        data class B<T>(override val a: T): A<T>()

        parameterTypes.register { B(it.toInt()) }

        val b: B<Int> = B(3)
        val a: A<Int> = b
        assertConverts("3", b)
        assertConverts("3", a)
    }

    @Test
    fun `Handles enums`() {
        val blue = Color.BLUE
        val red = Color.RED

        assertAll(
            { assertConverts("blue", blue) },
            { assertConverts("BLue", blue) },
            { assertConverts("red", red) },
        )
    }

    private inline fun <reified T> assertConverts(
        input: String,
        expected: T,
    ) {
        val actual = parameterTypes.get(T::class).parser(input)

        assertEquals(expected, actual)
    }

    private enum class Color {
        BLUE,
        RED,
    }
}
