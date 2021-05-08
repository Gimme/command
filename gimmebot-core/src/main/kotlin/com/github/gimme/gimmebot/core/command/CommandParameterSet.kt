package com.github.gimme.gimmebot.core.command

/**
 * Represents a set of [CommandParameter]s.
 *
 * The main purpose of this class is to supply efficient lookup methods.
 */
class CommandParameterSet(parameters: Collection<CommandParameter> = listOf()): Set<CommandParameter> {

    private val parameters: List<CommandParameter> = parameters.toList()
    private val parameterById: Map<String, CommandParameter> = parameters.map { it.id to it }.toMap()
    private val parameterByFlag: Map<Char, CommandParameter>

    init {
        parameterByFlag = mutableMapOf()
        parameters.reversed().forEach { param ->
            param.flags.forEach { flag -> parameterByFlag[flag] = param }
        }
    }

    override fun iterator() = parameters.iterator()

    /**
     * Returns the parameter corresponding to the given [id], or `null` if a parameter with such an [id] is not present
     * in this set.
     */
    operator fun get(id: String): CommandParameter? = parameterById[id]

    /**
     * Returns the parameter corresponding to the given [flag], or `null` if a parameter with such a [flag] is not
     * present in this set.
     */
    fun getByFlag(flag: Char): CommandParameter? = parameterByFlag[flag]

    /**
     * Returns if this set contains a parameter with the specified [id].
     */
    fun containsId(id: String): Boolean = parameterById.containsKey(id)

    override val size: Int
        get() = parameters.size

    override fun contains(element: CommandParameter): Boolean = containsId(element.id)

    override fun containsAll(elements: Collection<CommandParameter>): Boolean = elements.all { containsId(it.id) }

    override fun isEmpty(): Boolean = parameters.isEmpty()
}
