package dev.gimme.command.parameter

import dev.gimme.command.exception.ErrorCode
import dev.gimme.command.exception.UnsupportedParameterTypeException
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

/**
 * Handles globally registered/supported [ParameterType]s.
 */
object ParameterTypes {

    private val registeredTypes = mutableMapOf<KClass<*>, ParameterType<*>>()

    init {
        register<Any>(name = "String") { it }
        register(name = "String") { it }
        register(name = "Integer") { it.toIntOrNull() }
        register(name = "Number") { it.toDoubleOrNull() }
        register(name = "Boolean", values = { setOf("true", "false") }) {
            when {
                it.equals("true", true) || it == "1" -> true
                it.equals("false", true) || it == "0" -> false
                else -> null
            }
        }
    }

    /**
     * Returns the parameter type registered for the [klass].
     *
     * @throws UnsupportedParameterTypeException if the [klass] and any subtype to it is not registered
     * @see register
     */
    @Throws(UnsupportedParameterTypeException::class)
    fun get(klass: KClass<*>): ParameterType<*> = getOrNull(klass) ?: throw UnsupportedParameterTypeException(klass)

    /**
     * Returns the parameter type registered for the [klass], or null if the [klass] and any subtype to it is not
     * registered.
     *
     * @see register
     */
    private fun getOrNull(klass: KClass<*>): ParameterType<*>? {
        registeredTypes[klass]?.let { return it }
        getOrRegisterEnumType(klass)?.let { return it }

        registeredTypes.forEach {
            if (it.key.isSubclassOf(klass)) {
                put(klass, it.value)
                return it.value
            }
        }

        return null
    }

    /**
     * Registers a custom parameter type.
     *
     * [T] can then safely be used as the type for any command parameter.
     *
     * The basic types are registered by default, others need to be registered manually with this method.
     *
     * @param T             the type to be registered
     * @param klass         the class of the type to be registered
     * @param name          the display-name of the parameter type
     * @param values        returns all possible values the parameter type can have, or null if undefined
     * @param convertOrNull converts string to the parameter type, or returns null if unable to convert
     */
    fun <T : Any> register(
        klass: Class<T>,
        name: String = klass.simpleName ?: "?",
        values: (() -> Collection<String>)? = null,
        convertOrNull: (String) -> T?,
    ): ParameterType<T> {
        val parameterType = ParameterType(
            name = name,
            clazz = klass,
            values = values?.let { { it().toSet()} },
        ) { convertOrNull(it) ?: throw ErrorCode.INVALID_ARGUMENT.createException("\"$it\" (Not a valid $name)") }

        put(klass.kotlin, parameterType)
        return parameterType
    }

    /**
     * @see register
     */
    inline fun <reified T : Any> register(
        name: String = T::class.simpleName ?: "?",
        noinline values: (() -> Collection<String>)? = null,
        noinline convertOrNull: (String) -> T?,
    ): ParameterType<T> = register(T::class.java, name, values, convertOrNull)

    /**
     * Returns a parameter type for the [klass] if it is an enum, else null.
     *
     * Also registers the type if not already registered.
     */
    private fun <T : Any> getOrRegisterEnumType(klass: KClass<T>): ParameterType<*>? {
        return klass.java.enumConstants?.filterIsInstance(Enum::class.java)?.let { enumValues ->
            val name = klass.simpleName ?: "<Enum>"
            val values = enumValues.map { it.name }.toSet().let { { it } }

            @Suppress("UNCHECKED_CAST")
            val parse: (String) -> T = { string: String ->
                val value = enumValues.find { it.name.equals(string, ignoreCase = true) }
                    ?: throw ErrorCode.INVALID_ARGUMENT.createException("\"$string\" (Not a valid $name)")
                value as T
            }

            val parameterType = ParameterType(
                name = name,
                clazz = klass.java,
                values = values,
                parse = parse
            )

            put(klass, parameterType)
            parameterType
        }
    }

    /**
     * Associates the [parameterType] with the [klass] in the map.
     *
     * @return the previous value associated with the [klass], or null if the [klass] was not present in the map
     */
    fun put(klass: KClass<*>, parameterType: ParameterType<*>) = registeredTypes.put(klass, parameterType)
}
