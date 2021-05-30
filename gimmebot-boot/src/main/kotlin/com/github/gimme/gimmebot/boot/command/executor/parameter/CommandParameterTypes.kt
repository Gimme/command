package com.github.gimme.gimmebot.boot.command.executor.parameter

import com.github.gimme.gimmebot.core.command.CommandParameterType
import kotlin.reflect.KClassifier
import kotlin.reflect.KParameter
import kotlin.reflect.jvm.jvmErasure

/**
 * Returns a new command parameter type based on the [parameter].
 */
internal fun commandParameterTypeFrom(parameter: KParameter): CommandParameterType<*> {
    return map[parameter.type.classifier]
        ?: getEnumParameterType(parameter)
        ?: throw UnsupportedParameterException(parameter)
}

private val map = mapOf<KClassifier, CommandParameterType<*>>(
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

private object BOOLEAN : CommandParameterType<Boolean>("Boolean") {
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
        object : CommandParameterType<String>(
            name = parameter.type.jvmErasure.simpleName ?: "Enum",
            values = { enumValues }
        ) {
            override fun convertOrNull(input: Any) = enumValues.find { it.equals(input.toString(), ignoreCase = true) }
        }
    }
}
