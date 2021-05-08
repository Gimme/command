package com.github.gimme.gimmebot.core.command

/**
 * Represents something that can be uniquely identified by its group.
 */
interface Grouped {
    /**
     * A unique identifier with a group structure separated by ".".
     *
     * An example group could be: "foo.bar", where "foo" is the parent group and "bar" is added as the unique identifier
     * among elements inside that parent group.
     */
    val group: String

    /**
     *
     */
    fun getTokens(): List<String> = group.split(".")
}
