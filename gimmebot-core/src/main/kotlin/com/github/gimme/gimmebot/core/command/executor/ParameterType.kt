package com.github.gimme.gimmebot.core.command.executor

import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.jvm.jvmErasure

/**
 * Represents a supported parameter type for
 * [com.github.gimme.gimmebot.core.command.executor.CommandExecutor] commands.
 *
 * It is used to cast string input arguments to the corresponding type.
 */
enum class ParameterType(
    private val clazz: KClass<*>,
    private val arrayClass: KClass<*>,
    private val castArgFunction: (s: String) -> Any?,
    private val castArrayFunction: (col: Collection<*>) -> Any?,
) {
    STRING(String::class,
        Array<String>::class,
        { s -> s },
        { col -> col.map { v -> v as String }.toTypedArray() }),
    INTEGER(Int::class,
        IntArray::class,
        { s -> s.toIntOrNull() },
        { col -> col.map { v -> v as Int }.toIntArray() }),
    DOUBLE(Double::class,
        DoubleArray::class,
        { s -> s.toDoubleOrNull() },
        { col -> col.map { v -> v as Double }.toDoubleArray() }),
    BOOLEAN(Boolean::class,
        BooleanArray::class,
        { s ->
            when {
                s.equals("true", true) || s == "1" -> true
                s.equals("false", true) || s == "0" -> false
                else -> null
            }
        },
        { col -> col.map { v -> v as Boolean }.toBooleanArray() });

    /** Casts the given [value] to the correct type, or null on failure. */
    fun castArg(value: Any): Any? = if (clazz.isInstance(value)) value else castArgFunction(value.toString())

    /** Casts the given [collection] to the correct array type, or null on failure. */
    fun castArray(collection: Collection<*>): Any? = castArrayFunction(collection)

    companion object {
        /** Returns the corresponding [ParameterType] to the specified [parameter] or null if unsupported type. */
        fun fromClass(parameter: KParameter): ParameterType? {
            return values().find { v -> v.clazz == parameter.type.jvmErasure }
        }

        /** Returns the corresponding [ParameterType] to the specified array [parameter] or null if unsupported array type. */
        fun fromArrayClass(parameter: KParameter): ParameterType? {
            return values().find { v -> v.arrayClass == parameter.type.jvmErasure }
        }
    }
}
