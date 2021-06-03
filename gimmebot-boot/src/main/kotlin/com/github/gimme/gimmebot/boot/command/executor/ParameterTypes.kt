package com.github.gimme.gimmebot.boot.command.executor

import com.github.gimme.gimmebot.core.command.parameter.ParameterType
import com.github.gimme.gimmebot.core.command.parameter.PluralParameterType
import com.github.gimme.gimmebot.core.command.parameter.SingularParameterType
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.KTypeProjection
import kotlin.reflect.KVariance
import kotlin.reflect.full.createType
import kotlin.reflect.jvm.jvmErasure

/**
 * Handles globally registered/supported [ParameterType]s.
 */
@PublishedApi
internal object ParameterTypes {

    private val registeredTypes = mutableMapOf<KType, ParameterType<*>>()

    init {
        register { it }
        register(name = "Integer") { it.toIntOrNull() }
        register(name = "Number") { it.toDoubleOrNull() }
        register(name = "Boolean", values = { setOf("true", "false", "1", "0") }) {
            when {
                it.equals("true", true) || it == "1" -> true
                it.equals("false", true) || it == "0" -> false
                else -> null
            }
        }
    }

    /**
     * Returns the parameter type registered for the [type].
     *
     * Basic types are registered by default, others need to be registered manually.
     *
     * @see register
     */
    internal fun get(type: KType): ParameterType<*> {
        return registeredTypes[type]
            ?: getOrRegisterEnumType(type.jvmErasure)
            ?: throw UnsupportedParameterTypeException(type)
    }

    /**
     * Registers a custom parameter type.
     *
     * [T] can then be safely used for parameters in the reflective commands.
     *
     * This includes support for [Array], [List], [Set], [Collection] and [Iterable] versions of [T].
     *
     * @param T             the type to be registered
     * @param name          the display name of the parameter type
     * @param values        returns all possible values the parameter type can have, or null if undefined
     * @param errorMessage  message to be included if an input value is invalid and cannot be converted to the parameter
     * type
     * @param convertOrNull converts string input to the parameter type, or returns null if unable to convert
     */
    inline fun <reified T : Any> register(
        name: String = T::class.simpleName ?: "?",
        noinline values: (() -> Set<String>)? = null,
        errorMessage: String? = "Not a `$name`",
        crossinline convertOrNull: (String) -> T?,
    ): SingularParameterType<T> {
        val type = T::class.createType()
        val singularParameterType = SingularParameterType(name, values, errorMessage, { convertOrNull(it) })

        put(type, singularParameterType)

        registerPlural(singularParameterType) { it.toTypedArray() }
        registerPlural(singularParameterType) { it }
        registerPlural(singularParameterType) { it.toSet() }
        registerPlural<Collection<T>, T>(singularParameterType) { it }
        registerPlural<Iterable<T>, T>(singularParameterType) { it }

        return singularParameterType
    }

    /**
     * Registers a custom plural version of the [singularParameterType].
     *
     * @see register
     */
    inline fun <reified T, reified E : Any> registerPlural(
        singularParameterType: SingularParameterType<E>,
        type: KType = T::class.createType(listOf(KTypeProjection(KVariance.INVARIANT, E::class.createType()))),
        crossinline convertToType: (List<E>) -> T
    ): PluralParameterType<T> = registerPlural(singularParameterType.toPlural(convertToType), type)

    /**
     * Registers a custom [singularParameterType].
     *
     * @see register
     */
    inline fun <reified T> registerSingular(
        singularParameterType: SingularParameterType<T>,
        type: KType = T::class.createType(),
    ): SingularParameterType<T> {
        put(type, singularParameterType)
        return singularParameterType
    }

    /**
     * Registers a custom [pluralParameterType].
     *
     * @see register
     */
    inline fun <reified T> registerPlural(
        pluralParameterType: PluralParameterType<T>,
        type: KType = T::class.createType(),
    ): PluralParameterType<T> {
        put(type, pluralParameterType)
        return pluralParameterType
    }

    /**
     * Returns a [SingularParameterType] for the [kClass] if it is an enum, else null.
     *
     * Then registers the type if not already registered.
     */
    private fun getOrRegisterEnumType(kClass: KClass<*>): SingularParameterType<*>? {
        return kClass.java.enumConstants?.filterIsInstance(Enum::class.java)?.let { enumValues ->
            val name = kClass.simpleName ?: "<Enum>"
            val values = enumValues.map { it.name }.toSet().let { { it } }
            val convertFunction: (String) -> Enum<*>? =
                { input: Any -> enumValues.find { it.name.equals(input.toString(), ignoreCase = true) } }

            registerSingular(
                SingularParameterType(
                    name = name,
                    values = values,
                    convertOrNull = convertFunction
                ), kClass.createType()
            )
        }
    }

    @PublishedApi
    internal fun put(type: KType, parameterType: ParameterType<*>) = registeredTypes.put(type, parameterType)
}
