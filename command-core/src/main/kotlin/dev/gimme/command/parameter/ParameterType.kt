package dev.gimme.command.parameter

/**
 * Represents a command parameter type.
 *
 * @param T           the real type that this represents
 * @property name     the display-name of this type
 * @property clazz    the java class that this type represents
 * @property values   returns all possible values this type can have (in string form), or null if undefined
 * @property parse    converts a string to this parameter type, or throws an exception if it could not be converted
 */
data class ParameterType<T : Any>(
    val name: String,
    val clazz: Class<T>,
    val values: (() -> Set<String>)? = null,
    val parse: (String) -> T,
)
