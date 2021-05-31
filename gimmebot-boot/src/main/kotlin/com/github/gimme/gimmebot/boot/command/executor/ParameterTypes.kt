package com.github.gimme.gimmebot.boot.command.executor

import com.github.gimme.gimmebot.core.command.parameter.ParameterType
import com.github.gimme.gimmebot.core.command.parameter.PluralParameterType
import com.github.gimme.gimmebot.core.command.parameter.SingularParameterType
import kotlin.reflect.KClass
import kotlin.reflect.KClassifier
import kotlin.reflect.KParameter
import kotlin.reflect.KTypeProjection
import kotlin.reflect.KVariance
import kotlin.reflect.full.createType
import kotlin.reflect.jvm.jvmErasure

/**
 * Handles globally registered/supported [ParameterType]s.
 */
object ParameterTypes {

    private val INTEGER = SingularParameterType("Integer") { it.toIntOrNull() }
    private val DOUBLE = SingularParameterType("Number") { it.toDoubleOrNull() }
    private val BOOLEAN = SingularParameterType(
        name = "Boolean",
        values = { setOf("true", "false", "1", "0") },
    ) {
        when {
            it.equals("true", true) || it == "1" -> true
            it.equals("false", true) || it == "0" -> false
            else -> null
        }
    }

    @PublishedApi
    internal val registeredTypes = mutableMapOf<KClassifier, ParameterType<*>>(
        IntArray::class to PluralParameterType("Integers") { it.map { INTEGER.convert(it) }.toIntArray() },
        DoubleArray::class to PluralParameterType("Numbers") { it.map { DOUBLE.convert(it) }.toDoubleArray() },
        BooleanArray::class to PluralParameterType("Booleans") { it.map { BOOLEAN.convert(it) }.toBooleanArray() },
    )

    init {
        registerType { it }
        registerType("Integer") { it.toIntOrNull() }
        registerType("Number") { it.toDoubleOrNull() }
        registerType(
            "Boolean",
            values = { setOf("true", "false", "1", "0") },
            convertOrNull = BOOLEAN::convert
        )
    }

    /**
     * TODO: document parameters
     * Registers a custom command parameter type.
     *
     * This [T] type can then be safely used in [CommandExecutor] function declarations for automatic command parameter
     * generation. This includes support for arrays of this type as well.
     */
    inline fun <reified T> registerType(
        name: String = T::class.simpleName ?: "?",
        noinline values: (() -> Set<String>)? = null,
        errorMessage: String? = "Not a `$name`",
        crossinline convertOrNull: (String) -> T?,
    ) where T : Any {
        val type = T::class.createType()
        val arrayType = Array<T>::class.createType(arguments = listOf(KTypeProjection(KVariance.OUT, type)))

        val commandParameterType = SingularParameterType(name, values, errorMessage, { convertOrNull(it) })

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
     * Returns a new command parameter type based on the [kParameter].
     */
    internal fun from(kParameter: KParameter): ParameterType<*> {
        return registeredTypes[kParameter.type.classifier]
            ?: getEnumParameterType(kParameter)
            ?: throw UnsupportedParameterException(kParameter)
    }

    /**
     * Returns a [SingularParameterType] for the [parameter] if it is an enum.
     */
    private fun getEnumParameterType(parameter: KParameter): SingularParameterType<*>? {
        val enumClass: KClass<*> = parameter.type.jvmErasure
        val cls: Class<*> = try {
            Class.forName(enumClass.qualifiedName)
        } catch (e: Exception) {
            return null
        }
        val enumValues: Set<Enum<*>>? = cls.enumConstants?.filterIsInstance(Enum::class.java)?.toSet()

        return enumValues?.let {
            val name = enumClass.simpleName ?: "<Enum>"
            val values = enumValues.map { it.name }.toSet().let { { it } }
            val convertFunction: (String) -> Enum<*>? =
                { input: Any -> enumValues.find { it.name.equals(input.toString(), ignoreCase = true) } }

            SingularParameterType(
                name = name,
                values = values,
                convertOrNull = convertFunction
            )
        }
    }

    /**
     * Returns an [Array] version of the [singularParameterType], which can handle vararg parameters.
     */
    @PublishedApi
    internal inline fun <reified T> commandParameterTypeToArrayType(singularParameterType: SingularParameterType<T>):
            PluralParameterType<Array<T>> where T : Any {
        return PluralParameterType(
            name = singularParameterType.name + if (singularParameterType.name.endsWith("s")) "" else "s",
            values = singularParameterType.values
        ) { input -> input.map { singularParameterType.convert(it) }.toTypedArray() }
    }
}
