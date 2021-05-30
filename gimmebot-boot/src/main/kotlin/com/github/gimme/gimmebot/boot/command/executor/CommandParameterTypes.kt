package com.github.gimme.gimmebot.boot.command.executor

import com.github.gimme.gimmebot.core.command.parameter.CommandParameterType
import kotlin.reflect.KClass
import kotlin.reflect.KClassifier
import kotlin.reflect.KParameter
import kotlin.reflect.KTypeProjection
import kotlin.reflect.KVariance
import kotlin.reflect.full.createType
import kotlin.reflect.jvm.jvmErasure

/**
 * Registers a custom command parameter type. This [T] type can then be safely used in [CommandExecutor] function
 * declarations for automatic command parameter generation.
 */
inline fun <reified T> registerParameterType(commandParameterType: CommandParameterType<T>) where T : Any {
    val type = T::class.createType()
    val arrayType = Array<T>::class.createType(arguments = listOf(KTypeProjection(KVariance.OUT, type)))

    registeredTypes[type.classifier
        ?: throw IllegalArgumentException("Invalid command parameter type: ${commandParameterType.name}")] =
        commandParameterType

    registeredTypes.putIfAbsent(
        arrayType.classifier
            ?: throw IllegalArgumentException("Invalid command parameter array type: ${commandParameterType.name}"),
        commandParameterTypeToArrayType(commandParameterType)
    )
}

/**
 * Returns a new command parameter type based on the [parameter].
 */
internal fun commandParameterTypeFrom(parameter: KParameter): CommandParameterType<*> {
    return registeredTypes[parameter.type.classifier]
        ?: getEnumParameterType(parameter)
        ?: throw UnsupportedParameterException(parameter)
}

private val STRING = CommandParameterType("String") { it.toString() }

private val INTEGER = CommandParameterType("Integer") { it.toString().toIntOrNull() }

private val DOUBLE = CommandParameterType("Number") { it.toString().toDoubleOrNull() }

private val BOOLEAN = CommandParameterType(
    name = "Boolean",
    values = { setOf("true", "false", "1", "0") },
) {
        val s = it.toString()
        when {
            s.equals("true", true) || s == "1" -> true
            s.equals("false", true) || s == "0" -> false
            else -> null
        }
}

@PublishedApi
internal val registeredTypes = mutableMapOf<KClassifier, CommandParameterType<*>>(
    String::class to STRING,
    Int::class to INTEGER,
    Double::class to DOUBLE,
    Boolean::class to BOOLEAN,
    Array<String>::class to commandParameterTypeToArrayType(STRING),
    IntArray::class to CommandParameterType("Integers") { input ->
        (input as Collection<*>).map { INTEGER.convert(it!!) }.toIntArray()
    },
    DoubleArray::class to CommandParameterType("Numbers") { input ->
        (input as Collection<*>).map { DOUBLE.convert(it!!) }.toDoubleArray()
    },
    BooleanArray::class to CommandParameterType("Booleans") { input ->
        (input as Collection<*>).map { BOOLEAN.convert(it!!) }.toBooleanArray()
    },
)

/**
 * Returns a [CommandParameterType] for the [parameter] if it is an enum.
 */
private fun getEnumParameterType(parameter: KParameter): CommandParameterType<*>? {
    val enumClass: KClass<*> = parameter.type.jvmErasure
    val cls: Class<*>? = Class.forName(enumClass.qualifiedName)
    val enumValues: Set<Enum<*>>? = cls?.enumConstants?.filterIsInstance(Enum::class.java)?.toSet()

    return enumValues?.let {
        val name = enumClass.simpleName ?: "<Enum>"
        val values = enumValues.map { it.name }.toSet().let { { it } }
        val convertFunction: (Any) -> Enum<*>? =
            { input: Any -> enumValues.find { it.name.equals(input.toString(), ignoreCase = true) } }

        CommandParameterType(
            name = name,
            values = values,
            convertOrNull = convertFunction
        )
    }
}

/**
 * Returns an [Array] version of the [commandParameterType], which can handle vararg parameters.
 */
@PublishedApi
internal inline fun <reified E> commandParameterTypeToArrayType(commandParameterType: CommandParameterType<E>): CommandParameterType<Array<E>> where E : Any {
    return CommandParameterType(
        name = commandParameterType.name + if (commandParameterType.name.endsWith("s")) "" else "s",
        values = commandParameterType.values
    ) { input -> (input as Collection<*>).map { commandParameterType.convert(it!!) }.toTypedArray() }
}
