package com.github.gimme.gimmebot.core.command

import com.github.gimme.gimmebot.core.command.exception.UnsupportedParameterTypeException
import com.github.gimme.gimmebot.core.command.parameter.ParameterType
import com.github.gimme.gimmebot.core.command.parameter.PluralParameterType
import com.github.gimme.gimmebot.core.command.parameter.SingularParameterType
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.KTypeProjection
import kotlin.reflect.KVariance
import kotlin.reflect.full.createType
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.withNullability
import kotlin.reflect.jvm.jvmErasure

/**
 * Handles globally registered/supported [ParameterType]s.
 */
object ParameterTypes {

    private val registeredTypes = mutableMapOf<KType, ParameterType<*>>()

    init {
        register { it }
        val intType = register(name = "Integer") { it.toIntOrNull() }
        val doubleType = register(name = "Number") { it.toDoubleOrNull() }
        val booleanType = register(name = "Boolean", values = { setOf("true", "false", "1", "0") }) {
            when {
                it.equals("true", true) || it == "1" -> true
                it.equals("false", true) || it == "0" -> false
                else -> null
            }
        }
        registerPlural(intType) { it.toIntArray() }
        registerPlural(doubleType) { it.toDoubleArray() }
        registerPlural(booleanType) { it.toBooleanArray() }
    }

    /**
     * Returns the parameter type registered for the [type].
     *
     * @throws UnsupportedParameterTypeException if the [type] and any subtype to it is not registered
     * @see register
     */
    @Throws(UnsupportedParameterTypeException::class)
    fun get(type: KType): ParameterType<*> = getOrNull(type) ?: throw UnsupportedParameterTypeException(type)

    /**
     * Returns the parameter type registered for the [type], or null if the [type] and any subtype to it is not
     * registered.
     *
     * @see register
     */
    fun getOrNull(type: KType): ParameterType<*>? {
        val nonNullType = type.withNullability(false)

        registeredTypes[nonNullType]?.let { return it }
        getOrRegisterEnumType(nonNullType.jvmErasure)?.let { return it }

        registeredTypes.forEach {
            if (it.key.isSubtypeOf(nonNullType)) {
                put(nonNullType, it.value)
                return it.value
            }
        }

        return null
    }

    /**
     * Registers a custom parameter type.
     *
     * [T] can then be safely used for parameters in the reflective commands.
     *
     * Basic types are registered by default, others need to be registered manually with this.
     *
     * This includes support for [Array], [List], [Set], [Collection] and [Iterable] versions of [T].
     *
     * @param T             the type to be registered
     * @param name          the display name of the parameter type
     * @param values        returns all possible values the parameter type can have, or null if undefined
     * @param errorMessage  message to be included if an input value is invalid and cannot be converted to the parameter
     * type
     * @param typeArguments the type arguments to use if [T] is generic
     * @param convertOrNull converts string input to the parameter type, or returns null if unable to convert
     */
    inline fun <reified T : Any> register(
        name: String = T::class.simpleName ?: "?",
        noinline values: (() -> Set<String>)? = null,
        errorMessage: String? = "Not a `$name`",
        typeArguments: List<KType?> = emptyList(),
        crossinline convertOrNull: (String) -> T?,
    ): SingularParameterType<T> {
        val type: KType = T::class.createType(typeArguments.map {
            it?.let { KTypeProjection.invariant(it) } ?: KTypeProjection.STAR
        })
        val singularParameterType = SingularParameterType(name, values, errorMessage, { convertOrNull(it) })

        put(type, singularParameterType)

        registerPlural(singularParameterType, listOf(type)) { it.toTypedArray() }
        registerPlural(singularParameterType, listOf(type)) { it }
        registerPlural(singularParameterType, listOf(type)) { it.toSet() }
        registerPlural<Collection<T>, T>(singularParameterType, listOf(type)) { it }
        registerPlural<Iterable<T>, T>(singularParameterType, listOf(type)) { it }

        return singularParameterType
    }

    /**
     * Registers a custom plural version of the [singularParameterType] using the given [typeArguments].
     *
     * @see register
     */
    inline fun <reified T : Any, reified E : Any> registerPlural(
        singularParameterType: SingularParameterType<E>,
        typeArguments: List<KType?> = emptyList(),
        crossinline convertToType: (List<E>) -> T
    ): PluralParameterType<T> = registerPlural(singularParameterType.toPlural(convertToType), typeArguments)

    /**
     * Registers a custom [singularParameterType] using the given [typeArguments].
     *
     * @see register
     */
    inline fun <reified T : Any> registerSingular(
        singularParameterType: SingularParameterType<T>,
        typeArguments: List<KType?> = emptyList(),
    ): SingularParameterType<T> {
        val type: KType = T::class.createType(typeArguments.map { KTypeProjection(KVariance.INVARIANT, it) })
        put(type, singularParameterType)
        return singularParameterType
    }

    /**
     * Registers a custom [pluralParameterType] using the given [typeArguments].
     *
     * @see register
     */
    inline fun <reified T : Any> registerPlural(
        pluralParameterType: PluralParameterType<T>,
        typeArguments: List<KType?> = emptyList(),
    ): PluralParameterType<T> {
        val type: KType = T::class.createType(typeArguments.map { KTypeProjection(KVariance.INVARIANT, it) })
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

            val singularParameterType = SingularParameterType(
                name = name,
                values = values,
                convertOrNull = convertFunction
            )

            put(kClass.createType(), singularParameterType)
            singularParameterType
        }
    }

    fun put(type: KType, parameterType: ParameterType<*>) = registeredTypes.put(type, parameterType)
}
