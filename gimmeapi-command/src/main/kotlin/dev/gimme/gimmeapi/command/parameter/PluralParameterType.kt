package dev.gimme.gimmeapi.command.parameter

/**
 * A parameter type representing a group of values.
 *
 * @param T the real type that this represents
 */
class PluralParameterType<T : Any>(
    name: String,
    override val values: (() -> Set<String>)? = null,
    private val convertFunction: (Collection<String>) -> T
) : ParameterType<T> {

    override val name = "[$name]"
    override val singular = false
    override val errorMessage: String? = null

    override fun convert(input: Collection<String>): T = convertFunction(input)
}
