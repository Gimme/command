package com.github.gimme.gimmebot.core.command.parameter

/**
 * Represents an ordered set of [CommandParameter]s.
 *
 * The main purpose of this class is to supply efficient lookup methods.
 */
class CommandParameterSet(parameters: Collection<CommandParameter> = listOf()): MutableSet<CommandParameter> {

    private val parameters: MutableSet<CommandParameter> = parameters.toMutableSet()
    private val parameterById: MutableMap<String, CommandParameter> = parameters.map { it.id to it }.toMap().toMutableMap()
    private val parameterByFlag: MutableMap<Char, CommandParameter> = mutableMapOf()

    init {
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

    override fun add(element: CommandParameter): Boolean {
        if (!parameters.add(element)) return false
        parameterById[element.id] = element
        element.flags.forEach { flag -> parameterByFlag[flag] = element }

        return true
    }

    override fun addAll(elements: Collection<CommandParameter>): Boolean {
        if (!parameters.addAll(elements)) return false
        parameterById.putAll(elements.map { it.id to it }.toMap() as MutableMap<String, CommandParameter>)
        elements.reversed().forEach { param ->
            param.flags.forEach { flag -> parameterByFlag[flag] = param }
        }

        return true
    }

    override fun clear() {
        parameters.clear()
        parameterById.clear()
        parameterByFlag.clear()
    }

    override fun remove(element: CommandParameter): Boolean {
        if (!parameters.remove(element)) return false
        parameterById.remove(element.id)
        parameterByFlag.values.remove(element)

        return true
    }

    override fun removeAll(elements: Collection<CommandParameter>): Boolean {
        if (!parameters.removeAll(elements)) return false
        parameterById.values.removeAll(elements)
        parameterByFlag.values.removeAll(elements)

        return true
    }

    override fun retainAll(elements: Collection<CommandParameter>): Boolean {
        if (!parameters.retainAll(elements)) return false
        parameterById.values.retainAll(elements)
        parameterByFlag.values.retainAll(elements)

        return true
    }
}
