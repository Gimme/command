package com.github.gimme.gimmebot.boot.command.executor

import com.github.gimme.gimmebot.core.command.parameter.CommandParameterType
import kotlin.reflect.KClassifier
import kotlin.reflect.KParameter
import kotlin.reflect.full.createType
import kotlin.reflect.jvm.jvmErasure

/**
 * Registers a custom command parameter type. This [T] type can then be safely used in [CommandExecutor] function
 * declarations for automatic command parameter generation.
 */
inline fun <reified T> registerParameterType(commandParameterType: CommandParameterType<T>) where T : Any {
    val type = T::class.createType()
    val arrayType = Array<T>::class.createType()

    registeredTypes[type.classifier
        ?: throw IllegalArgumentException("Invalid command parameter type: ${commandParameterType.name}")] =
        commandParameterType

    registeredTypes[arrayType.classifier
        ?: throw IllegalArgumentException("Invalid command parameter array type: ${commandParameterType.name}")] =
        object : CommandParameterType<Array<T>>(
            name = commandParameterType.name + if (commandParameterType.name.endsWith("s")) "" else "s",
            values = commandParameterType.values
        ) {
            override fun convertOrNull(input: Any) =
                (input as Collection<*>).map { commandParameterType.convert(it!!) }.toTypedArray()
        }
}

/**
 * Returns a new command parameter type based on the [parameter].
 */
internal fun commandParameterTypeFrom(parameter: KParameter): CommandParameterType<*> {
    return registeredTypes[parameter.type.classifier]
        ?: getEnumParameterType(parameter)
        ?: throw UnsupportedParameterException(parameter)
}

@PublishedApi
internal val registeredTypes = mutableMapOf<KClassifier, CommandParameterType<*>>(
    String::class to STRING,
    Int::class to INTEGER,
    Double::class to DOUBLE,
    Boolean::class to BOOLEAN,
    Array<String>::class to object : CommandParameterType<Array<String>>("Strings") {
        override fun convertOrNull(input: Any) =
            (input as Collection<*>).map { STRING.convert(it!!) }.toTypedArray()
    },
    IntArray::class to object : CommandParameterType<IntArray>("Integers") {
        override fun convertOrNull(input: Any) =
            (input as Collection<*>).map { INTEGER.convert(it!!) }.toIntArray()
    },
    DoubleArray::class to object : CommandParameterType<DoubleArray>("Numbers") {
        override fun convertOrNull(input: Any) =
            (input as Collection<*>).map { DOUBLE.convert(it!!) }.toDoubleArray()
    },
    BooleanArray::class to object : CommandParameterType<BooleanArray>("Booleans") {
        override fun convertOrNull(input: Any) =
            (input as Collection<*>).map { BOOLEAN.convert(it!!) }.toBooleanArray()
    },
)

private object STRING : CommandParameterType<String>("String") {
    override fun convertOrNull(input: Any) = input.toString()
}

private object INTEGER : CommandParameterType<Int>("Integer") {
    override fun convertOrNull(input: Any) = input.toString().toIntOrNull()
}

private object DOUBLE : CommandParameterType<Double>("Number") {
    override fun convertOrNull(input: Any) = input.toString().toDoubleOrNull()
}

private object BOOLEAN : CommandParameterType<Boolean>(
    name = "Boolean",
    values = { setOf("true", "false", "1", "0") },
) {
    override fun convertOrNull(input: Any): Boolean? {
        val s = input.toString()
        return when {
            s.equals("true", true) || s == "1" -> true
            s.equals("false", true) || s == "0" -> false
            else -> null
        }
    }
}

private fun getEnumParameterType(parameter: KParameter): CommandParameterType<String>? {
    val enumClassName = parameter.type.jvmErasure.qualifiedName
    val cls = Class.forName(enumClassName)
    val enumValues = cls?.enumConstants?.filterIsInstance(Enum::class.java)?.map { it.name }?.toSet()

    return enumValues?.let {
        val values = { it }
        object : CommandParameterType<String>(
            name = parameter.type.jvmErasure.simpleName ?: "Enum",
            values = values
        ) {
            override fun convertOrNull(input: Any) = enumValues.find { it.equals(input.toString(), ignoreCase = true) }
        }
    }
}
