package com.github.gimme.gimmebot.core.command

import com.github.gimme.gimmebot.core.command.exceptions.UnsupportedParameterException
import kotlin.reflect.KClassifier
import kotlin.reflect.KParameter

/**
 * Represents a command parameter, which helps define how arguments can be passed to the command execution function.
 *
 * @property id          the unique identifier of this parameter
 * @property displayName the display name of this parameter
 * @property type        the type of this parameter
 * @property suggestions suggested argument values
 * @property description the description of this parameter
 * @property vararg      if this is a vararg parameter
 * @property optional    if this parameter is optional
 * @property flags       available shorthand flags representing this parameter
 */
data class CommandParameter(
    val id: String,
    val displayName: String,
    val type: CommandParameterType<*>,
    val suggestions: Set<String> = setOf(),
    val description: String? = null,
    val vararg: Boolean = false,
    val optional: Boolean = false,
    val flags: Set<Char> = setOf()
)

/**
 * Represents a command parameter type used to manage input argument values.
 *
 * @property name the display name of this parameter type.
 */
abstract class CommandParameterType<T>(val name: String) where T : Any {

    /**
     * Converts the [input] to this parameter type.
     *
     * @throws CommandException if the [input] is invalid
     */
    @Throws(CommandException::class)
    fun convert(input: Any): T = convertOrNull(input) ?: throw ErrorCode.INVALID_ARGUMENT.createException(input)

    /**
     * Converts the [input] to this parameter type, or null if failed to convert.
     *
     * @throws CommandException if the internal conversion failed
     */
    @Throws(CommandException::class)
    abstract fun convertOrNull(input: Any): T?

    companion object {
        private val map = mutableMapOf<KClassifier, CommandParameterType<*>>()

        init {
            map[String::class] = STRING
            map[Int::class] = INTEGER
            map[Double::class] = DOUBLE
            map[Boolean::class] = BOOLEAN
            map[Array<String>::class] = object : CommandParameterType<Array<String>>("Strings") {
                override fun convertOrNull(input: Any) =
                    (input as Collection<*>).map { STRING.convert(it!!) }.toTypedArray()
            }
            map[IntArray::class] = object : CommandParameterType<IntArray>("Integers") {
                override fun convertOrNull(input: Any) =
                    (input as Collection<*>).map { INTEGER.convert(it!!) }.toIntArray()
            }
            map[DoubleArray::class] = object : CommandParameterType<DoubleArray>("Numbers") {
                override fun convertOrNull(input: Any) =
                    (input as Collection<*>).map { DOUBLE.convert(it!!) }.toDoubleArray()
            }
            map[BooleanArray::class] = object : CommandParameterType<BooleanArray>("Booleans") {
                override fun convertOrNull(input: Any) =
                    (input as Collection<*>).map { BOOLEAN.convert(it!!) }.toBooleanArray()
            }
        }

        /**
         * Returns a new command parameter type based on the [parameter].
         */
        fun from(parameter: KParameter): CommandParameterType<*> = map[parameter.type.classifier]
            ?: throw UnsupportedParameterException(parameter)
    }

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
}
