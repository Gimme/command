package com.github.gimme.gimmebot.core.command

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import kotlin.reflect.KTypeProjection
import kotlin.reflect.full.createType

internal class ParameterTypesTest {

    private val parameterTypes: ParameterTypes = ParameterTypes

    @Test
    fun `Converts basic singular types`() {
        assertAll(
            { assertConverts("abc", "abc") },
            { assertConverts("1", 1) },
            { assertConverts("0.5", 0.5) },
        )

        data class A(val a: String)
        parameterTypes.register { A(it) }
        assertConverts("abc", A("abc"))
    }

    @Test
    fun `Converts basic plural types`() {
        assertAll(
            {
                assertConvertsPlural(
                    listOf("a", "b"),
                    listOf("a", "b"),
                    KTypeProjection.invariant(String::class.createType()),
                )
            },
            {
                assertConvertsPlural(
                    listOf("1"),
                    listOf(1),
                    KTypeProjection.invariant(Int::class.createType()),
                )
            },
            {
                assertConvertsPlural(
                    listOf("0.5", "-100"),
                    listOf(0.5, -100.0),
                    KTypeProjection.invariant(Double::class.createType()),
                )
            },
        )
    }

    @Test
    fun `Converts generic types`() {
        data class A<T>(val a: T)

        parameterTypes.register<A<*>>(typeArguments = listOf(null)) { A(it.toDouble()) }
        assertConverts(
            "3",
            A(3.0),
            KTypeProjection.STAR,
        )

        val intType = Int::class.createType()
        parameterTypes.register(typeArguments = listOf(intType)) { A(it.toInt()) }
        assertConverts(
            "3",
            A(3),
            KTypeProjection.invariant(intType),
        )
    }

    @Test
    fun `Fully registers custom type`() {
        data class A(val a: String)
        val argument = KTypeProjection.invariant(A::class.createType())
        val input = listOf("1", "2")
        val result = input.map { A(it) }
        val val1 = "1"

        parameterTypes.register { A(it) }

        assertConverts(val1, A(val1))
        assertConvertsPlural(input, result, argument)
        assertConvertsPlural(input, result.toSet(), argument)
        assertConvertsPlural<Collection<A>>(input, result, argument)
        assertConvertsPlural<Iterable<A>>(input, result, argument)
    }

    @Test
    fun `Handles subtypes`() {
        abstract class A<T> {
            abstract val a: T
        }
        data class B<T>(override val a: T): A<T>()

        val intType = Int::class.createType()
        parameterTypes.register(typeArguments = listOf(intType)) { B(it.toInt()) }

        val b: B<Int> = B(3)
        val a: A<Int> = b
        assertConverts("3", b, KTypeProjection.invariant(intType))
        assertConverts("3", a, KTypeProjection.invariant(intType))
    }

    private inline fun <reified T> assertConverts(
        input: String,
        expected: T,
        vararg arguments: KTypeProjection,
    ) {
        val actual = parameterTypes.get(T::class.createType(arguments.toList())).convert(listOf(input))

        assertEquals(expected, actual)
    }

    private inline fun <reified T> assertConvertsPlural(
        input: List<String>,
        expected: T,
        vararg arguments: KTypeProjection,
    ) {
        val actual = parameterTypes.get(T::class.createType(arguments.toList())).convert(input)

        assertEquals(expected, actual)
    }
}
