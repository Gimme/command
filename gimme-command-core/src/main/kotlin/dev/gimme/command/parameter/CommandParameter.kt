package dev.gimme.command.parameter

/**
 * Represents a command parameter, which helps define how arguments can be passed to the command execution function.
 *
 * @property id                 the unique identifier of this parameter
 * @property name               the name of this parameter
 * @property type               the type of this parameter
 * @property suggestions        gets all suggested argument values
 * @property description        the description of this parameter
 * @property form               the form in which this parameter hold its type
 * @property flags              available shorthand flags representing this parameter
 * @property optional           whether this parameter is optional
 * @property defaultValue       the default value used if this parameter is optional
 * @property defaultValueString the string representation of the default value
 */
data class CommandParameter(
    val id: String,
    val name: String,
    val type: ParameterType<*>,
    val suggestions: () -> Set<String> = type.values ?: { setOf() },
    val description: String? = null,
    val form: Form = Form.VALUE,
    val flags: MutableSet<Char> = mutableSetOf(),
    val optional: Boolean = false,
    val defaultValue: Any? = null,
    val defaultValueString: String? = null
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CommandParameter

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    /**
     * The form in which a parameter holds its values of its type.
     */
    enum class Form(
        /**
         * If the form is a collection.
         */
        val isCollection: Boolean = true
    ) {

        /**
         * Just the value itself, without container.
         */
        VALUE(isCollection = false),

        /**
         * Holds values in a list.
         */
        LIST,

        /**
         * Holds values in a set.
         */
        SET,
    }
}
